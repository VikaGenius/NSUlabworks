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

typedef int (*CompareFunc)(size_t, size_t);

typedef struct _Arg {
    Storage* list;
    CompareFunc cmp;
} Arg;

int iter_count_less = 0;
int iter_count_great = 0;
int iter_count_equal = 0;

int iter_swap_count = 0;

void* monitor() {
    while(1) {
        sleep(1);
        printf("-----------------------------------\n");
        printf("Iter count less: %d \nIter count great: %d\nIter count equal: %d\nIter SWAP count: %d \n", iter_count_less, iter_count_great, iter_count_equal, iter_swap_count);

    }
}

void generate_random_string(char *str, int min_length, int max_length) {
    const char charset[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
    int len = rand() % (max_length - min_length + 1) + min_length; 

    for (int i = 0; i < len - 1; ++i) {
        int index = rand() % (strlen(charset) - 1); 
        str[i] = charset[index];
    }

    str[len - 1] = '\0';
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

//когда у нас 4 элемента, мы сначала захватываем лочку на первых трех элементах, затем, на 2-4
//как только перекинули указатель first->next на третий элемент, захватываем лочку на 2-4 элементах
void random_nodes_swap(Storage* list) {
    assert(list->head);
        
    pthread_mutex_lock(&list->head->sync);
    Node* first = list->head;
    Node* second = NULL;
    Node* third = NULL;

    while (first->next) {
        pthread_mutex_lock(&first->next->sync);
        second = first->next;
        if (second->next) {
            pthread_mutex_lock(&second->next->sync);
            third = second->next;
            if (third != NULL && rand() % 2 == 1) { 
                first->next = third;
                second->next = third->next;
                third->next = second;
            }
            pthread_mutex_unlock(&third->sync);
        }
        pthread_mutex_unlock(&first->sync);
        first = second;
    }
    pthread_mutex_unlock(&first->sync);
}

void* swap_thread_func(void* arg) {
    Storage* list = (Storage*)arg;

    while (1) {
        random_nodes_swap(list);
        iter_swap_count++;
    }

    return NULL;
}

int less(size_t a, size_t b) {
    return a < b;
}

int great(size_t a, size_t b) {
    return a > b;
}

int equal(size_t a, size_t b) {
    return a == b;
}

void count_good_pair(Storage* list, int (*cmp)(size_t, size_t)) {
    if (list == NULL) {
        return;
    }

    int cgp = 0; // считаем хорошие пары)))
    Node* current = list->head;
    while (current && current->next) {
        pthread_mutex_lock(&current->sync);
        size_t len1 = strlen(current->value);

        Node* other = current->next;
        while(other) {
            pthread_mutex_lock(&other->sync);

            size_t len2 = strlen(other->value);
            if (cmp(len1, len2)) {
                cgp++;
            }

            pthread_mutex_unlock(&other->sync);
            other = other->next;
        }
        pthread_mutex_unlock(&current->sync);
        current = current->next;
    }
    //printf("COUNT GOOD PAIR: %d \n", cgp);
}

void* count_thread_func(void* args) {
    Arg* arg = (Arg*)args;
    CompareFunc compare = arg->cmp;
    while (1) {
        sleep(1);
        count_good_pair(arg->list, compare);

        if (compare == less) {
            iter_count_less++;
        } else if (compare == equal) {
            iter_count_equal++;
        } else if (compare == great) {
            iter_count_great++;
        }
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
    Storage* list = init(10000);
    CompareFunc cmps[4];
    cmps[1] = less;
    cmps[2] = great;
    cmps[0] = equal;

    pthread_t tid[7];
    int err;
    for (int i = 0; i < 3; i++) {
        err = pthread_create(&tid[i], NULL, swap_thread_func, list);
        if (err) {
            printf("main: pthread_create() failed: %s\n", strerror(err));
            return -1;
        }	
    }

    for (int i = 3; i < 6; i++) {
        Arg* arg = (Arg*)malloc(sizeof(Arg));
        arg->list = list;
        arg->cmp = cmps[i % 3];
        err = pthread_create(&tid[i], NULL, count_thread_func, arg);
        if (err) {
            printf("main: pthread_create() failed: %s\n", strerror(err));
            return -1;
        }	
    }

    err = pthread_create(&tid[6], NULL, monitor, list);
        if (err) {
            printf("main: pthread_create() failed: %s\n", strerror(err));
            return -1;
        }	

    for (int i = 0; i < 7; i++) {
        pthread_join(tid[i], NULL);
    }
    
    destroy(list);
}