#!/usr/bin/env python3

import os
from collections import Counter
from datetime import datetime, timedelta
from pymongo import MongoClient, UpdateOne
from pymongo.errors import BulkWriteError

MONGO_HOST = os.getenv("MONGO_HOST")
MONGO_BATCH_SIZE = 100
DB_NAME = "selcMsCore"
DELEGATION_COLLECTION = "Delegations"
INSTITUTION_COLLECTION = "Institution"

START_DATE = datetime(2024, 1, 1, 0, 0, 0, 0)
DATE_INCREMENT = timedelta(seconds=5)

class AnsiColors:
    ERROR = '\033[91m'
    SUCCESS = '\033[92m'
    ENDC = '\033[0m'

def format_date_string(dt):
    if isinstance(dt, str):
        return dt
    return dt.isoformat(timespec='microseconds') + "Z"

def bulkWrite(updateBatch, collection):
    try:
        result = collection.bulk_write(updateBatch, ordered=False)
        return {
            "countInserted": result.inserted_count,
            "countUpserted": result.upserted_count,
            "countMatched": result.matched_count,
            "countModified": result.modified_count,
            "countRemoved": result.deleted_count
        }
    except BulkWriteError as bwe:
        result = bwe.details
        print(AnsiColors.ERROR, f"BulkWriteError {result}", AnsiColors.ENDC)
        return {
            "countInserted": result["nInserted"],
            "countUpserted": result["nUpserted"],
            "countMatched": result["nMatched"],
            "countModified": result["nModified"],
            "countRemoved": result["nRemoved"]
        }

def build_institution_maps(institutions):
    onboarding_map = {}
    fallback_map = {}

    for inst in institutions:
        inst_id = inst["_id"]
        fallback_map[inst_id] = inst.get("createdAt")
        for entry in inst.get("onboarding", []):
            product_id = entry.get("productId")
            if product_id and "createdAt" in entry:
                onboarding_map[(inst_id, product_id)] = entry["createdAt"]

    return onboarding_map, fallback_map

def main():
    client = MongoClient(MONGO_HOST)
    db = client[DB_NAME]
    delegation_collection = db[DELEGATION_COLLECTION]
    institution_collection = db[INSTITUTION_COLLECTION]

    cursor = delegation_collection.find({"createdAt": {"$exists": False}}, batch_size=MONGO_BATCH_SIZE)
    totalDelegationsCount = delegation_collection.count_documents({})
    delegations = list(cursor)

    institution_ids = list({d["from"] for d in delegations})
    institutions = list(institution_collection.find({"_id": {"$in": institution_ids}}))
    onboarding_map, fallback_map = build_institution_maps(institutions)

    updateBatch = []
    current_date = START_DATE
    bulkCounters = {
        "countInserted": 0,
        "countUpserted": 0,
        "countMatched": 0,
        "countModified": 0,
        "countRemoved": 0
    }
    doc_count = 0

    for doc in delegations:
        doc_id = doc["_id"]
        inst_id = doc["from"]
        product_id = doc.get("productId")

        createdAt = onboarding_map.get((inst_id, product_id)) or fallback_map.get(inst_id)
        used_fallback = False
        if not createdAt:
            createdAt = current_date
            current_date += DATE_INCREMENT
            used_fallback = True

        if used_fallback:
            print(AnsiColors.ERROR + f"[{doc_id}] No dates found in institution, using start date with increment." + AnsiColors.ENDC)

        createdAt_str = format_date_string(createdAt)
        update = UpdateOne({"_id": doc_id}, {"$set": {"createdAt": createdAt_str}})
        updateBatch.append(update)
        doc_count += 1

        if len(updateBatch) >= MONGO_BATCH_SIZE:
            result = bulkWrite(updateBatch, delegation_collection)
            updateBatch = []
            bulkCounters = dict(Counter(bulkCounters) + Counter(result))
            print(f"{doc_count} / {totalDelegationsCount} - {bulkCounters}", end="\r")

    if updateBatch:
        result = bulkWrite(updateBatch, delegation_collection)
        bulkCounters = dict(Counter(bulkCounters) + Counter(result))

    print(f"{doc_count} / {totalDelegationsCount} - {bulkCounters}")
    client.close()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print("\n Manual interruption detected. Exiting.")