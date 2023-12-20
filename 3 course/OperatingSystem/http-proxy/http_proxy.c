#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <pthread.h>
#include <netdb.h>

#define MAX_BUFFER_SIZE 1024 * 4

int setting_proxy_socket() {
    struct sockaddr_in proxy_addr;
    int proxy_socket;

    proxy_socket = socket(AF_INET, SOCK_STREAM, 0);
    if (proxy_socket < 0) {
        perror("Ошибка при создании прокси-сокета");
        exit(EXIT_FAILURE);
    }

    //настраиваем адрес и порт прокси-сокета
    proxy_addr.sin_family = AF_INET;
    proxy_addr.sin_addr.s_addr = INADDR_ANY;
    proxy_addr.sin_port = htons(8080);

    // привязываем прокси-сокет к адресу и порту
    if (bind(proxy_socket, (struct sockaddr*)&proxy_addr, sizeof(proxy_addr)) < 0) {
        perror("Ошибка при привязке прокси-сокета");
        exit(EXIT_FAILURE);
    }
    // Ожидаем соединений от клиентов
    if (listen(proxy_socket, 5) < 0) {
        perror("Ошибка при ожидании соединений");
        exit(EXIT_FAILURE);
    }

    printf("[*] Прокси слушает на порту 80\n");

    return proxy_socket;
}

void get_host(char* buffer, char* host) {
    char *first_line = strtok(buffer, "\n");
    char *url = strtok(first_line + 4, " ");
    
    int port = 8080;

    sscanf(url, "http://%[^:/]:%d", host, &port);
}

int connect_and_get_server_socket(char *host) {
    struct addrinfo hints, *result, *p;

    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_UNSPEC; 
    hints.ai_socktype = SOCK_STREAM;

    char* port = "80";
    if (getaddrinfo(host, port, &hints, &result) != 0) {
        perror("Ошибка при разрешении DNS");
        exit(EXIT_FAILURE);
    }

    int server_socket;
    for (p = result; p != NULL; p = p->ai_next) {
        server_socket = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
        if (server_socket == -1) {
            continue; // ошибка при создании сокета, пробуем следующий адрес
        }

        if (connect(server_socket, p->ai_addr, p->ai_addrlen) != -1) {
            //inet_ntop
            break;
        } else {
            perror("Ошибка при установлении соединения с удаленным сервером");
            close(server_socket);
            return -1;
        }
    }

    printf("CONNECT\n");
    freeaddrinfo(result);
    return server_socket;
}


void handle_client(int client_socket) {
    char buffer[MAX_BUFFER_SIZE];
    ssize_t bytes_received;

    // получаем запрос от клиента
    bytes_received = recv(client_socket, buffer, sizeof(buffer), 0);
    if (bytes_received < 0) {
        perror("Ошибка при чтении из клиентского сокета");
        close(client_socket);
        return;
    }

    printf("RECV: %ld\n", bytes_received);

    char host[MAX_BUFFER_SIZE];
    get_host(buffer, host);
    printf("HOST: %s \n", host);
    int server_socket = connect_and_get_server_socket(host);

    if (send(server_socket, buffer, bytes_received, 0) < 0) {
        perror("Ошибка при отправке данных на сервер");
        close(client_socket);
        close(server_socket);
        return;
    }

    printf("SEND MSG TO SERVER\n");

    // Получаем данные от сервера и пересылаем их клиенту
    while ((bytes_received = recv(server_socket, buffer, sizeof(buffer), 0)) > 0) {
        if (send(client_socket, buffer, bytes_received, 0) < 0) {
            perror("Ошибка при отправке данных клиенту");
            break;
        }
    }

    printf("RECIVE MSG AND SEND TO CLIENT \n");

    // Закрываем соединения
    close(client_socket);
    close(server_socket);

}

void *client_handler(void *arg) {
    int client_socket = *((int *)arg);

    printf("CLIENT HANDLER\n");

    handle_client(client_socket);
    
    return NULL;
}

void accept_connection(int proxy_socket) {
    int client_socket;
    struct sockaddr_in client_addr;
    socklen_t client_addr_len = sizeof(client_addr);
    pthread_t thread_id;

    while (1) {
        //принимаем соединение от клиента
        client_socket = accept(proxy_socket, (struct sockaddr *)&client_addr, &client_addr_len);
        if (client_socket < 0) {
            perror("Ошибка при принятии соединения");
            continue;
        }

        printf("[*] Принято соединение от: %s:%d\n", inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));

        // Создаем новый поток для обработки клиента
        if (pthread_create(&thread_id, NULL, client_handler, (void *)&client_socket) < 0) {
            perror("Ошибка при создании потока для обработки клиента");
            close(client_socket);
            continue;
        }

        // Освобождаем ресурсы потока после завершения
        pthread_detach(thread_id);
    }

}

int main() {
    int proxy_socket = setting_proxy_socket();
    accept_connection(proxy_socket);
    close(proxy_socket);

    return 0;
}
