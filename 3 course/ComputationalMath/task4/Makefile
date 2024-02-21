CXX = g++
CXXFLAGS = -g -Wall
GTEST_DIR = /usr/lib/x86_64-linux-gnu
GTEST_LIB = $(GTEST_DIR)/libgtest.a
GTEST_INCLUDE = -I$(GTEST_DIR)/include

SRC = my_test.cpp
OBJS = $(SRC:.cpp=.o)

all: my_test

my_test: $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^ $(GTEST_LIB) -pthread

%.o: %.cpp
	$(CXX) $(CXXFLAGS) $(GTEST_INCLUDE) -c $<

clean:
	rm -f my_test $(OBJS)

