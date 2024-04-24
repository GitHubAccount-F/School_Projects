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

#include "./MemIndex.h"

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>

#include "libhw1/CSE333.h"
#include "libhw1/HashTable.h"
#include "libhw1/LinkedList.h"


///////////////////////////////////////////////////////////////////////////////
// Internal-only helpers
//
// These functions use the MI_ prefix instead of the MemIndex_ prefix,
// to indicate that they should not be considered part of the MemIndex's
// public API.

// Comparator usable by LinkedList_Sort(), which implements an increasing
// order by rank over SearchResult's.  If the caller is interested in a
// decreasing sort order, they should invert the return value.
static int MI_SearchResultComparator(LLPayload_t e1, LLPayload_t e2) {
  SearchResult* sr1 = (SearchResult*) e1;
  SearchResult* sr2 = (SearchResult*) e2;

  if (sr1->rank > sr2->rank) {
    return 1;
  } else if (sr1->rank < sr2->rank) {
    return -1;
  } else {
    return 0;
  }
}

// Deallocator usable by LinkedList_Free(), which frees a list of
// of DocPositionOffset_t.  Since these offsets are stored inline (ie, not
// malloc'ed), there is nothing to do in this function.
static void MI_NoOpFree(LLPayload_t ptr) { }

// Deallocator usable by HashTable_Free(), which frees a LinkedList of
// DocPositionOffset_t (ie, our posting list).  We use these LinkedLists
// in our WordPostings.
static void MI_PostingsFree(HTValue_t ptr) {
  LinkedList* list = (LinkedList*) ptr;
  LinkedList_Free(list, &MI_NoOpFree);
}

// Deallocator used by HashTable_Free(), which frees a WordPostings.  A
// MemIndex consists of a HashTable of WordPostings (ie, this is the top-level
// structure).
static void MI_ValueFree(HTValue_t ptr) {
  WordPostings* wp = (WordPostings*) ptr;

  free(wp->word);
  HashTable_Free(wp->postings, &MI_PostingsFree);

  free(wp);
}

///////////////////////////////////////////////////////////////////////////////
// MemIndex implementation

MemIndex* MemIndex_Allocate(void) {
  // Happily, HashTables dynamically resize themselves, so we can start by
  // allocating a small hashtable.
  HashTable* index = HashTable_Allocate(16);
  Verify333(index != NULL);
  return index;
}

void MemIndex_Free(MemIndex* index) {
  HashTable_Free(index, &MI_ValueFree);
}

int MemIndex_NumWords(MemIndex* index) {
  return HashTable_NumElements(index);
}

void MemIndex_AddPostingList(MemIndex* index, char* word, DocID_t doc_id,
                             LinkedList* postings) {
  HTKey_t key = FNVHash64((unsigned char*) word, strlen(word));
  HTKeyValue_t mi_kv, postings_kv, unused;
  WordPostings* wp;
  // STEP 1.
  // Remove this early return.  We added this in here so that your unittests
  // would pass even if you haven't finished your MemIndex implementation.
  


  // First, we have to see if the passed-in word already exists in
  // the inverted index.
  if (!HashTable_Find(index, key, &mi_kv)) {
    // STEP 2.
    // No, this is the first time the inverted index has seen this word.  We
    // need to prepare and insert a new WordPostings structure.  After
    // malloc'ing it, we need to:
    //   (1) find existing memory or allocate new memory for the WordPostings'
    //       word field (hint: remember that this function takes ownership
    //       of the passed-in word).
    //   (2) allocate a new hashtable for the WordPostings' docID->postings
    //       mapping.
    //   (3) insert the the new WordPostings into the inverted index (ie, into
    //       the "index" table).
      wp = (WordPostings*) malloc(sizeof(WordPostings));
      wp->word = word;
      // Create a hashtable with 1 bucket initially for the document
      wp->postings = HashTable_Allocate(1);
      HTKeyValue_t temp;
      temp.key = key;
      temp.value = wp;
      HashTable_Insert(index, temp, NULL);




  } else {
    // Yes, this word already exists in the inverted index.  There's no need
    // to insert it again.

    // Instead of allocating a new WordPostings, we'll use the one that's
    // already in the inverted index.
    wp = (WordPostings*) mi_kv.value;

    // Ensure we don't have hash collisions (two different words that hash to
    // the same key, which is very unlikely).
    Verify333(strcmp(wp->word, word) == 0);

    // Now we can free the word (since the caller gave us ownership of it).
    free(word);
  }

  // At this point, we have a WordPostings struct which represents the posting
  // list for this word.  Add this new document's postings to it.

  // Verify that this document is not already in the posting list.
  Verify333(!HashTable_Find(wp->postings, doc_id, &postings_kv));

  // STEP 3.
  // Insert a new entry into the wp->postings hash table.
  // The entry's key is this docID and the entry's value
  // is the "postings" (ie, word positions list) we were passed
  // as an argument.
  unused.key = doc_id;
  unused.value = postings;
  HashTable_Insert(wp->postings, unused, &postings_kv);
}

LinkedList* MemIndex_Search(MemIndex* index, char* query[], int query_len) {
  //printf("here12\n");
  LinkedList* ret_list;
  HTKeyValue_t kv;
  WordPostings* wp;
  HTKey_t key;
  int i;

  // If the user provided us with an empty search query, return NULL
  // to indicate failure.
  if (query_len == 0) {
    return NULL;
  }

  // STEP 4.
  // The most interesting part of Part C starts here...!
  //
  // Look up the first query word (ie, query[0]) in the inverted index.  For
  // each document that matches, allocate and initialize a SearchResult
  // structure (the initial computed rank is the number of times the word
  // appears in that document).  Finally, append the SearchResult onto ret_list.
  key = FNVHash64((unsigned char*) query[0], strlen(query[0]));
  if (!HashTable_Find(index, key, &kv)) {
    return NULL;
  }

  //printf("here13\n");
  wp = (WordPostings*) kv.value;
  HTIterator* itr = HTIterator_Allocate(wp->postings);
  DocID_t tableKey = 0;  // represents current key in hashtable
  printf("num of element in table %d\n", HashTable_NumElements(wp->postings));
  ret_list = LinkedList_Allocate();
  // Checks to see if any document contains the first word, 
  // if not, then return NULL
  if (!HTIterator_Get(itr, &kv)) {
    return NULL;
  }
  tableKey = kv.key;
  printf("tablekey = %ld\n",tableKey);
  bool check = HTIterator_Next(itr);
  // Tests if only one document was present, in which
  // we only add 1 element to the list
  if (!check) {
    SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
    temp->doc_id = tableKey;
    temp->rank = 1;
    LinkedList_Append(ret_list, (LLPayload_t) temp);
  }
  while (HTIterator_Get(itr, &kv)) {
    check = HTIterator_Next(itr); // potentially skipping
    if (!check || kv.key != tableKey) {
      if (!check && kv.key != tableKey) {
        printf("tablekey = %ld\n",tableKey);
        printf("key = %ld\n",kv.key);
        SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
        SearchResult* temp2 = (SearchResult*) malloc(sizeof(SearchResult));
        DocID_t storeKey = (DocID_t)kv.key;
        temp->doc_id = tableKey;
        HashTable_Find(wp->postings, tableKey, &kv);
        temp->rank = LinkedList_NumElements(kv.value);
        temp2->doc_id = storeKey;
        temp2->rank = 1;
        LinkedList_Append(ret_list, (LLPayload_t) temp);
        LinkedList_Append(ret_list, (LLPayload_t) temp2);
      } else {
        SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
        DocID_t storeKey = (DocID_t)kv.key;
        temp->doc_id = tableKey;
        HashTable_Find(wp->postings, tableKey, &kv);
        temp->rank = LinkedList_NumElements(kv.value);
        LinkedList_Append(ret_list, (LLPayload_t) temp);
        tableKey = storeKey;
      }
    }
  }
















  /*
  if (!check) {
    SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
    temp->doc_id = tableKey;
    temp->rank = 1;
    LinkedList_Append(ret_list, (LLPayload_t) temp);
  }

  while (HTIterator_Get(itr, &kv)) {
    check = HTIterator_Next(itr);
    if (!check || kv.key != tableKey) {
      if (check && kv.key != tableKey) {
        SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
        temp->doc_id = tableKey;
        DocID_t storeKey = (DocID_t)kv.key;
        HashTable_Find(wp->postings, tableKey, &kv);
        temp->rank = LinkedList_NumElements(kv.value);
        LinkedList_Append(ret_list, (LLPayload_t) temp);
        tableKey = storeKey;
      } else if(!check && kv.key != tableKey) {
          printf("key = %ld\n",tableKey);
          SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
          SearchResult* temp2 = (SearchResult*) malloc(sizeof(SearchResult));
          temp->doc_id = tableKey;
          HashTable_Find(wp->postings, tableKey, &kv);
          DocID_t storeKey = (DocID_t)kv.key;
          temp->rank = LinkedList_NumElements(kv.value);
          temp2->doc_id = storeKey;
          temp2->rank = 1;
          LinkedList_Append(ret_list, (LLPayload_t) temp);
          LinkedList_Append(ret_list, (LLPayload_t) temp);
      } else if (!check && kv.key == tableKey) {
          SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
          temp->doc_id = tableKey;
          HashTable_Find(wp->postings, tableKey, &kv);
          temp->rank = LinkedList_NumElements(kv.value);
          LinkedList_Append(ret_list, (LLPayload_t) temp);
      }
    }
    // else nothing 
  }
  */
  













  // free iterator once we are done with it
  HTIterator_Free(itr);

  // Great; we have our search results for the first query
  // word.  If there is only one query word, we're done!
  // Sort the result list and return it to the caller.
  if (query_len == 1) {
    LinkedList_Sort(ret_list, false, &MI_SearchResultComparator);
    return ret_list;
  }

  // OK, there are additional query words.  Handle them one
  // at a time.
  //printf("here15\n");
  for (i = 1; i < query_len; i++) {
    LLIterator *ll_it;
    int j, num_docs;

    // STEP 5.
    // Look up the next query word (query[i]) in the inverted index.
    // If there are no matches, it means the overall query
    // should return no documents, so free retlist and return NULL.
    key = FNVHash64((unsigned char*) query[i], strlen(query[i]));
    if (!HashTable_Find(index, key, &kv)) {
      LinkedList_Free(ret_list, &free);
      return NULL;
    }
    //printf("here16\n");


    // STEP 6.
    // There are matches.  We're going to iterate through
    // the docIDs in our current search result list, testing each
    // to see whether it is also in the set of matches for
    // the query[i].
    //
    // If it is, we leave it in the search
    // result list and we update its rank by adding in the
    // number of matches for the current word.
    //
    // If it isn't, we delete that docID from the search result list.
    //printf("potential\n");
    ll_it = LLIterator_Allocate(ret_list);
    Verify333(ll_it != NULL);
    num_docs = LinkedList_NumElements(ret_list);
    printf("num_docs = %d\n", num_docs);
    for (j = 0; j < num_docs; j++) {
      // get current iterator element
      SearchResult* temp = NULL;
      LLIterator_Get(ll_it, (LLPayload_t) &temp);
      //printf("her1\n");
      // Retrieves the WordPosting, which has the hash table corresponding 
      // to the word
      WordPostings* wp2 = kv.value;
      if (!HashTable_Find(wp2->postings, temp->doc_id, &kv)) {
        // current word doesn't have that document, so remove it from the list
        //printf("here\n");
        LLIterator_Remove(ll_it, &free);
      } else {
        // update rank
        //printf("her2\n");
        temp->rank += LinkedList_NumElements(kv.value);
      }
      LLIterator_Next(ll_it);
  
    }
    LLIterator_Free(ll_it);


    // We've finished processing this current query word.  If there are no
    // documents left in our result list, free retlist and return NULL.
    if (LinkedList_NumElements(ret_list) == 0) {
      LinkedList_Free(ret_list, (LLPayloadFreeFnPtr)free);
      return NULL;
    }
  }

  // Sort the result list by rank and return it to the caller.
  LinkedList_Sort(ret_list, false, &MI_SearchResultComparator);
  return ret_list;
}
/*  else {  // when there is 2 or more documents associated with the word
    while (HTIterator_Get(itr, &kv)) {
      bool temp = HTIterator_Next(itr);
      // Either at end of table, or onto the next key in the table
      if (!temp || kv.key != tableKey) {
        // if we are at the end of the table and the key remains the same
        if (!temp &&  kv.key != tableKey) {
          SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
          SearchResult* temp2 = (SearchResult*) malloc(sizeof(SearchResult));
          temp->doc_id = tableKey;
          temp->rank = count;
          temp2->doc_id = kv.key;
          temp2->rank = 1;
          LinkedList_Append(ret_list, (LLPayload_t) temp2);
          LinkedList_Append(ret_list, (LLPayload_t) temp);

        } else {
          SearchResult* temp = (SearchResult*) malloc(sizeof(SearchResult));
          temp->doc_id = tableKey;
          temp->rank = count;
          LinkedList_Append(ret_list, (LLPayload_t) temp);
          tableKey = kv.key;
          count = 1;
        }
        // nothing important happened, we move onto next element
        // in the table
      } else {
       count++;
      }
    }
`*/