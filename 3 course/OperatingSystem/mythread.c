#define _GNU_SOURCE
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <linux/futex.h>
#include <stdatomic.h>
#include <sched.h>

#include <sys/mman.h>
#include <string.h>
#include <errno.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdint.h>

#include <signal.h>
#include <pthread.h>

#define PAGE 4096
#define STACK_SIZE PAGE*8

typedef void *(*start_routine_t)(void*);

typedef struct _mythread {
    int mythread_id;
    start_routine_t start_routine;
    void* arg;
    void* retval;
    volatile int joined;
    volatile int exited;

} mythread_struct_t;

void* create_stack(off_t size, int thread_id) {
  void* stack;

  stack = mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_STACK | MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
  if (!stack) {
    printf("Error: stack = NULL\n");
    return NULL;
  }

  printf("Stack ptr#: %p\n", stack);

  return stack;
}

void wait_mythread(volatile int* lock) {
    const int one = 1;
    int err;

    while (1) {
        if (atomic_compare_exchange_strong(lock, &one, 1)) {
            return;
        }

        err = syscall(SYS_futex, &lock, FUTEX_WAIT, 0, NULL, NULL, 0);
        if (err == -1 && errno != EAGAIN) {
            printf("Futex FUTEX_WAIT failed: %s\n", strerror(errno));
            abort();
        }
    }
}

void wake_mythread(volatile int* lock) {
    const int zero = 0;
    int err; 
   
    if (atomic_compare_exchange_strong(lock, &zero, 1)) {
        err = syscall(SYS_futex, &lock, FUTEX_WAKE, 1, NULL, NULL, 0);
        if (err == -1) {
            printf("Futex FUTEX_WAKE failed: %s\n", strerror(errno));
            abort();
        }
    }
}

int mythread_startup(void* arg) {
    mythread_struct_t* mythread = (mythread_struct_t*)arg;

    mythread->retval = mythread ->start_routine(mythread->arg);
    //mythread->exited = 1;
    //меняем значение в функции ниже
    wake_mythread(&mythread->exited);

    //wait until join (тот самый futex!)
    wait_mythread(&mythread->joined);

    return 0;
}

void destroy(mythread_struct_t* mythread) {
    munmap((void*)mythread, STACK_SIZE);
}

int mythread_join(uintptr_t mytid, void** retval) {
    mythread_struct_t* mythread = (mythread_struct_t*)mytid;

    //wait until thread ends
    wait_mythread(&mythread->exited);

    printf("thread join: the thread %d finished\n", mythread->mythread_id);

    *retval = mythread->retval;

    //mythread->joined = 1;
    //меняю значение в функции ниже
    wake_mythread(&mythread->joined);

    destroy(mythread);

    return 0;
}

int mythread_create(uintptr_t* mytid, start_routine_t start_routine, void* arg) {
    static int thread_num = 0;
    mythread_struct_t* mythread;
    int child_pid;
    void* child_stack;

    thread_num++;

    printf("mythread_create: creating thread %d\n", thread_num);

    child_stack = create_stack(STACK_SIZE, thread_num);
    mprotect(child_stack + sizeof(mythread_struct_t), PAGE, PROT_NONE);

    mythread = (mythread_struct_t*)(child_stack + sizeof(mythread_struct_t));
    mythread->mythread_id = thread_num;
    mythread->start_routine = start_routine;
    mythread->arg = arg;
    mythread->retval = NULL;
    mythread->joined = 0;
    mythread->exited = 0;

    child_pid = clone(mythread_startup, child_stack + STACK_SIZE, CLONE_VM | CLONE_FILES | CLONE_SYSVSEM|  CLONE_THREAD | CLONE_SIGHAND | SIGCHLD, (void*)mythread);
    if (child_pid == -1) {
        printf("clone failed: %s\n", strerror(errno));
        exit(-1);
    }

    *mytid = (uintptr_t)mythread;

    return 0;
}

void* mythread_func (void* arg) {
    char* str = (char*)arg;
    for(int i = 0; i < 5; i++) {
        printf("hello:\n %s\n", str);
        sleep(1);
    }
    return (void*)"solnishko luchistoe lubit skakat`";
}

int main() {
    uintptr_t tid;
    void* retval;

    printf("main [%d %d %d]\n", getpid(), getppid(), gettid());

    mythread_create(&tid, mythread_func, "hello from main");
    sleep(10);
    mythread_join(tid, &retval);

    printf("main [%d %d %d] thread returned '%s'\n", getpid(), getppid(), gettid(), (char*)retval);

    return 0;
}