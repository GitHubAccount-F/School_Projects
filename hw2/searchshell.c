/*
 * Copyright ©2024 Hannah C. Tang.  All rights reserved.  Permission is
 * hereby granted to students registered for University of Washington
 * CSE 333 for use solely during Spring Quarter 2024 for purposes of
 * the course.  No other use, copying, distribution, or modification
 * is permitted without prior written consent. Copyrights for
 * third-party components of this work must be honored.  Instructors
 * interested in reusing these course materials should contact the
 * author.
 */

// Feature test macro for strtok_r (c.f., Linux Programming Interface p. 63)
#define _XOPEN_SOURCE 600
#define MAX_LENGTH 512  // Maximum length of input string


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>

#include "libhw1/CSE333.h"
#include "./CrawlFileTree.h"
#include "./DocTable.h"
#include "./MemIndex.h"

//////////////////////////////////////////////////////////////////////////////
// Helper function declarations, constants, etc
static void Usage(void);

// Input: str - a query, size - a return parameter
// Output: Returns an array of strings. Sets size to the number of
// elements in the array. If str does not contain any words, then NULL
// is returned and size is set to 0.
static char** TokenizeString(char *str, int* size);

// Input: str - a string
// Output: Returns nothing. Only modifies string given by converting it to
// lower case.
static void StringToLowercase(char *str);

// Input: query - an array of strings, size - number of elements in the array
// Output: Frees all elements in the array, and the array itself
static void FreeQuery(char** query, int size);

// Input: list - a query which is an array of strings,
//         size - number of elements in array,
//        index - a reference to a MemIndex table,
//        table - a reference to a DocTable storing
//        file names
// Output: Iterates through query, printing all file names and their
// rank that fit the query.
static void PrintFiles(char** list, int size, MemIndex* index, DocTable* table);


//////////////////////////////////////////////////////////////////////////////
// Main
int main(int argc, char** argv) {
  if (argc != 2) {
    Usage();
  }

  // Implement searchshell!  We're giving you very few hints
  // on how to do it, so you'll need to figure out an appropriate
  // decomposition into functions as well as implementing the
  // functions.  There are several major tasks you need to build:
  //
  //  - Crawl from a directory provided by argv[1] to produce and index
  //  - Prompt the user for a query and read the query from stdin, in a loop
  //  - Split a query into words (check out strtok_r)
  //  - Process a query against the index and print out the results
  //
  // When searchshell detects end-of-file on stdin (cntrl-D from the
  // keyboard), searchshell should free all dynamically allocated
  // memory and any other allocated resources and then exit.
  //
  // Note that you should make sure the fomatting of your
  // searchshell output exactly matches our solution binaries
  // to get full points on this part.

  // Step 1, crawl through the directory given
  DocTable* table;
  MemIndex* index;
  // Process directory
  bool check = CrawlFileTree(argv[1], &table, &index);
  if (!check) {
    printf("Give a correct file/directory.\n");
    return EXIT_FAILURE;
  }

  // Step 2, prompt the user for a query and convert it to a array of words
  // MAXIMUM PROMPT SIZE = 512 bytes
  printf("Indexing '%s'\n", argv[1]);
  char input[MAX_LENGTH];
  char *result;
  while (1) {
    printf("enter query:\n");
    result = fgets(input, sizeof(input), stdin);  // Read input line by line
    // checks results
    if (result == NULL) {  // we detected an EOF/ctrl-D or an error
      if (!feof(stdin)) {  // for when theres an actual error
        perror("fgets error");
      }
      printf("shutting down...\n");
      break;
    } else {
      // Process the input string
      // We want to first turn this result into an array of strings
      int size;
      char ** store = TokenizeString(result, &size);
      if (size == 0) {
        // We found no documents, so continue to next iteration
        FreeQuery(store, size);
        continue;
      } else {
        // To correct some error from tokenizing, only applies to
        // the last word
        char* lastElement = store[size - 1];

        lastElement[strlen(lastElement) - 1] = '\0';
      }
      // Get all documents that match the query
      PrintFiles(store, size, index, table);
      // Iteration ended, so we free query structure and
      // the list storing the doc files
      FreeQuery(store, size);
    }
  }
  // Free the structures we created in the beginning
  MemIndex_Free(index);
  DocTable_Free(table);


  return EXIT_SUCCESS;
}


//////////////////////////////////////////////////////////////////////////////
// Helper function definitions

static void Usage(void) {
  fprintf(stderr, "Usage: ./searchshell <docroot>\n");
  fprintf(stderr,
          "where <docroot> is an absolute or relative " \
          "path to a directory to build an index under.\n");
  exit(EXIT_FAILURE);
}

void FreeQuery(char** query, int size) {
  for (int i = 0; i < size; i++) {
    free(query[i]);  // free each word
  }
    free(query);  // free overall structure
}

static void StringToLowercase(char *str) {
  for (int i = 0; i < strlen(str); i++) {
    if (isalpha(str[i])) {
      str[i] = tolower(str[i]);
    }
  }
}


static char** TokenizeString(char *string, int* size) {
  StringToLowercase(string);
  char* str = string;
  char delimiters[] = " ";  // Delimiters: space
  char *token, *saveptr;
  int count = 0;
  char** words = NULL;
  // Begin converting string to tokens
  token = strtok_r(str, delimiters, &saveptr);
  // Iterate over tokens
  while (token != NULL) {
    // Allocate space for each word in query
    words = (char**) realloc(words, (count+1) * sizeof(char*));
    int length = strlen(token);
    token[length] = '\0';  // ensures each word ends ina null character
    words[count] = (char*) malloc(strlen(token) + 1);
    strncpy(words[count], token, strlen(token) + 1);  // copy in string
    // Get next token
    token = strtok_r(NULL, delimiters, &saveptr);
    count++;
  }
  *size = count;
  return words;
}

static void PrintFiles(char** list, int size, MemIndex* index,
                      DocTable* table) {
  // Get all documents that match the query
  LinkedList* output = MemIndex_Search(index, list, size);
  if (output == NULL) {
    // We found no documents matching query
    return;
  }
  // Create an iterator to go through all the documents corresponding to query
  LLIterator* itr = LLIterator_Allocate(output);
  while (LLIterator_IsValid(itr)) {
    SearchResult* temp;
    LLIterator_Get(itr, (LLPayload_t*)&temp);
    // Get filename using docId
    char* name = DocTable_GetDocName(table, temp->doc_id);
    // output
    printf("  %s (%d)\n", name, temp->rank);
    // free
    LLIterator_Next(itr);
  }
  LLIterator_Free(itr);
  LinkedList_Free(output, &free);
}
