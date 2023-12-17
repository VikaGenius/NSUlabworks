#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <assert.h>
#include <pthread.h> 
#define SIZE 100

typedef struct _Node {
    char value[SIZE];
    struct _Node* next;
    pthread_mutex_t sync;
} Node;

typedef struct _Storage {
    Node *head;
} Storage;


void generate_random_string(char *str, int min_length, int max_length) {
    const char charset[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
    int len = rand() % (max_length - min_length + 1) + min_length; 

    for (int i = 0; i < len - 1; ++i) {
        int index = rand() % (strlen(charset) - 1); 
        str[i] = charset[index];
    }

    str[len - 1] = '\0'; // Завершаем строку нулевым символом
}

void append(Storage* list, char* data) {

    Node* tmp = (Node*)malloc(sizeof(Node));
    assert(tmp);
    memcpy(tmp->value, data, strlen(data));
    pthread_mutex_init(&tmp->sync, NULL);

    Node* current_node = list->head;

    if (current_node == NULL) {
        list->head = tmp;
        return;
    } 
    
    while(current_node->next != NULL) {
        current_node = current_node->next;
    }
    current_node->next = tmp;
}

Storage* init(int count) {
    Storage* list = (Storage*)malloc(sizeof(Storage));
    assert(list);

    for (int i = 0; i < count; i++) {
        char data[SIZE];
        generate_random_string(data, 1, SIZE);
        append(list, data);
    }

    return list;
}

// void random_nodes_swap(Storage* list) {
//     Node* first = list->first;
//     Node* second = first->next;
//     if (first == NULL || second == NULL) {
//         return;
//     } else if (first && first->next && second->next == NULL) {
//         pthread_mutex_lock(&first->sync);
        
//         pthread_mutex_lock(&second->sync);
//         second->next = first;
//         first->next = NULL;
//         return;
//     }

//     Node *second, *third;
//     while(first && first->next && first->next->next) {
//         pthread_mutex_lock(&first->sync);
//         pthread_mutex_lock(&first->next->sync);
//         second = first->next;
//         pthread_mutex_lock(&second->next->sync);
//         third = second->next;

//         first->next = third;
//         pthread_mutex_unlock(&first->sync);
//         first = second;
//         if (third->next != NULL) {
//             pthread_mutex_lock(&third->next->sync);
//         }
//         second->next = third->next;
//         third->next = second;

//         pthread_mutex_unlock(&second->sync);
//         pthread_mutex_unlock(&third->sync);
//         if (third->next != NULL) {
//             pthread_mutex_unlock(&third->next->sync);
//         }
        
//     }
// }

void random_nodes_swap(Storage* list) {
    pthread_mutex_lock(&list->head->sync);
    Node* first = list->head;
    Node* second;

    // todo: fix 
    if (first == NULL || second == NULL) {
        pthread_mutex_unlock(&first->sync);
        return;
    }

    if (first && first->next) {     
        pthread_mutex_lock(&first->next->sync);
        second = first->next;

        first->next = second->next;
        second->next = first;
        first = second;
        list->head = second;   
        pthread_mutex_unlock(&first->next->sync);
    }

    Node *third;
    Node *forth;
    while(first && first->next && first->next->next) {
        pthread_mutex_lock(&first->next->sync);
        second = first->next;
        pthread_mutex_lock(&second->next->sync);
        third = second->next;

        first->next = third;
        pthread_mutex_unlock(&first->sync);
        first = third;
        pthread_mutex_unlock(&second->sync);
        forth = third->next;
        if (forth) {
            pthread_mutex_lock(&third->next->sync);
        }

        second->next = third->next;
        third->next = second;

        pthread_mutex_unlock(&third->sync);
        if (forth != NULL) {
            pthread_mutex_unlock(&forth->sync);
        }        
        pthread_mutex_lock(&first->sync);
    }

    pthread_mutex_unlock(&first->sync);
}

void* swap_thread_func(void* arg) {
    Storage* list = (Storage*)arg;
    while(1) {
        sleep(1);
        printf("One step of swapper\n");

        printf("List--------------------------\n");
        for (Node* curr = list->head; curr ; curr = curr->next) {
            printf("%zu \n", strlen(curr->value));
        }

        random_nodes_swap(list);
    }
}

void destroy(Storage* list) {
    Node* current_node = list->head;
    Node* next_node;

    while (current_node != NULL) {
        next_node = current_node->next;
        free(current_node);
        current_node = next_node;
    }

    free(list);
}

int main() {
    Storage* list = init(9);

    pthread_t tid[4];
    int err;
    for (int i = 0; i < 3; i++) {
        err = pthread_create(&tid[i], NULL, swap_thread_func, list);
        if (err) {
            printf("main: pthread_create() failed: %s\n", strerror(err));
            return -1;
        }	
    }

    for (int i = 0; i < 3; i++) {
        pthread_join(tid[i], NULL);
    }
    
    destroy(list);
}