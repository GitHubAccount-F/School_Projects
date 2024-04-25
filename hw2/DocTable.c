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
#include "./DocTable.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "libhw1/CSE333.h"
#include "libhw1/HashTable.h"

#define HASHTABLE_INITIAL_NUM_BUCKETS 2

// This structure represents a DocTable; it contains two hash tables, one
// mapping from document id to document name, and one mapping from
// document name to document id.
struct doctable_st {
  HashTable* id_to_name;  // mapping document id to document name
  HashTable* name_to_id;  // mapping document name to document id
  DocID_t    max_id;      // max doc ID allocated so far
};

DocTable* DocTable_Allocate(void) {
  DocTable* dt = (DocTable*) malloc(sizeof(DocTable));
  Verify333(dt != NULL);

  dt->id_to_name = HashTable_Allocate(HASHTABLE_INITIAL_NUM_BUCKETS);
  dt->name_to_id = HashTable_Allocate(HASHTABLE_INITIAL_NUM_BUCKETS);
  dt->max_id = 1;  // we reserve max_id = 0 for the invalid docID

  return dt;
}

// Input: A string representing the path to a document
// Output: Returns a hash key for that path
static HTKey_t CalculateHashKey(char* doc_name) {
  size_t length  = strlen(doc_name);
  HTKey_t key = FNVHash64((unsigned char*)doc_name, length);
  return key;
}

void DocTable_Free(DocTable* table) {
  Verify333(table != NULL);

  // STEP 1.
  HashTable_Free(table->id_to_name, &free);
  HashTable_Free(table->name_to_id, &free);



  free(table);
}

int DocTable_NumDocs(DocTable* table) {
  Verify333(table != NULL);
  return HashTable_NumElements(table->id_to_name);
}

DocID_t DocTable_Add(DocTable* table, char* doc_name) {
  char *doc_copy;
  DocID_t *doc_id;
  DocID_t* res;
  HTKeyValue_t kv, old_kv;
  HTKey_t hashKey;

  Verify333(table != NULL);

  // STEP 2.
  // Check to see if the document already exists.  Then make a copy of the
  // doc_name and allocate space for the new ID
  // Uses function to calculate hashkey for file name
  hashKey = CalculateHashKey(doc_name);
  if (HashTable_Find(table->name_to_id, hashKey, &old_kv)) {
    // We found the document, so we return it's DocID
    res = (DocID_t*)old_kv.value;
    return *res;
  }
  // Case when it was not found
  // malloc space for docId and word on the heap so that it
  // can't be lost
  doc_id =  (DocID_t*) malloc(sizeof(DocID_t));
  doc_copy = (char*) malloc((strlen(doc_name)) + 1);
  // copy over word to the heap
  strncpy(doc_copy, doc_name, strlen(doc_name) + 1);
  *doc_id = table->max_id;
  table->max_id++;





  // STEP 3.
  // Set up the key/value for the id->name mapping, and do the insert.
  kv.key = (HTKey_t) *doc_id;
  kv.value = (HTValue_t) doc_copy;
  // Verifies that this is false, i.e. the first time we add this to the table
  bool output1 = HashTable_Insert(table->id_to_name, kv, &old_kv);
  Verify333(!output1);


  // STEP 4.
  // Set up the key/value for the name->id, and/ do the insert.
  // Be careful about how you calculate the key for this mapping.
  // You want to be sure that how you do this is consistent with
  // the provided code.

  kv.key = hashKey;
  kv.value = doc_id;
  HashTable_Insert(table->name_to_id, kv, &old_kv);


  return *doc_id;
}

DocID_t DocTable_GetDocID(DocTable* table, char* doc_name) {
  HTKey_t key;
  HTKeyValue_t kv;
  DocID_t* res;

  Verify333(table != NULL);
  Verify333(doc_name != NULL);

  // STEP 5.
  // Try to find the passed-in doc in name_to_id table.
  // Uses function to calculate hashkey for file name
  key = CalculateHashKey(doc_name);

  if (HashTable_Find(table->name_to_id, key, &kv)) {
    // We found the document, so now we return it's
    // associated docID
    res = (DocID_t*)kv.value;
    return *res;
  }
  // case when document was not ever found
  return INVALID_DOCID;
}

char* DocTable_GetDocName(DocTable* table, DocID_t doc_id) {
  HTKeyValue_t kv;

  Verify333(table != NULL);
  Verify333(doc_id != INVALID_DOCID);

  // STEP 6.
  // Lookup the doc_id in the id_to_name table,
  // and either return the string (i.e., the (char *)
  // saved in the value field for that key) or
  // NULL if the key isn't in the table.
  if (HashTable_Find(table->id_to_name, (HTKey_t)doc_id, &kv)) {
    return (char*) kv.value;
  }


  return NULL;  // you may want to change this
}

HashTable* DT_GetIDToNameTable(DocTable* table) {
  Verify333(table != NULL);
  return table->id_to_name;
}

HashTable* DT_GetNameToIDTable(DocTable* table) {
  Verify333(table != NULL);
  return table->name_to_id;
}
