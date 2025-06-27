#!/usr/bin/env python3

import os

from pymongo import MongoClient, UpdateOne
from pymongo.errors import BulkWriteError

from collections import Counter

MONGO_HOST = os.getenv("MONGO_HOST")
MONGO_BATCH_SIZE = 100
INSTITUTION_DB = "selcMsCore"
INSTITUTION_COLLECTION = "Institution"

class AnsiColors:
    WARNING = '\033[93m'
    ERROR = '\033[91m'
    ENDC = '\033[0m'

def getInstitutionTypesUpdate(institutionDoc, institutionCollection):
    institutionId = institutionDoc["_id"]
    if "onboarding" not in institutionDoc:
        print(AnsiColors.WARNING, f"Institution {institutionId} without onboarding node", AnsiColors.ENDC)
        return None
    institutionType = institutionDoc["institutionType"] if "institutionType" in institutionDoc else None
    origin = institutionDoc["origin"] if "origin" in institutionDoc else None
    originId = institutionDoc["originId"] if "originId" in institutionDoc else None
    fieldsToSet = {}
    fieldsToUnset = {}
    # institutionType
    if institutionType:
        fieldsToSet["onboarding.$[].institutionType"] = institutionType
    else:
        fieldsToUnset["onboarding.$[].institutionType"] = ""
    # origin
    if origin:
        fieldsToSet["onboarding.$[].origin"] = origin
    else:
        fieldsToUnset["onboarding.$[].origin"] = ""
    # originId
    if originId:
        fieldsToSet["onboarding.$[].originId"] = originId
    else:
        fieldsToUnset["onboarding.$[].originId"] = ""
    # return update operation
    return UpdateOne({"_id": institutionId}, {"$set": fieldsToSet, "$unset": fieldsToUnset})

def bulkWrite(updateBatch, institutionCollection):
    try:
        result = institutionCollection.bulk_write(updateBatch, ordered=False)
        return { "countInserted": result.inserted_count, "countUpserted": result.upserted_count, "countMatched": result.matched_count, "countModified": result.modified_count, "countRemoved": result.deleted_count }
    except BulkWriteError as bwe:
        result = bwe.details
        print(AnsiColors.ERROR, f"BulkWriteError {result}", AnsiColors.ENDC)
        return { "countInserted": result["nInserted"], "countUpserted": result["nUpserted"], "countMatched": result["nMatched"], "countModified": result["nModified"], "countRemoved": result["nRemoved"] }

def main():
    client = MongoClient(MONGO_HOST)
    db = client[INSTITUTION_DB]
    institutionCollection = db[INSTITUTION_COLLECTION]

    totalInstitutionCount = institutionCollection.count_documents({})
    checkedInstitutionCount = 0
    bulkCounters = { "countInserted": 0, "countUpserted": 0, "countMatched": 0, "countModified": 0, "countRemoved": 0 }

    updateBatch = []
    for institutionDoc in institutionCollection.find({}, batch_size=MONGO_BATCH_SIZE):
        update = getInstitutionTypesUpdate(institutionDoc, institutionCollection)
        checkedInstitutionCount += 1
        if update:
            updateBatch.append(update)
        if len(updateBatch) >= MONGO_BATCH_SIZE:
            result = bulkWrite(updateBatch, institutionCollection)
            updateBatch = []
            bulkCounters = dict(Counter(bulkCounters) + Counter(result))
            print(f"{checkedInstitutionCount} / {totalInstitutionCount} - {bulkCounters}", end="\r")

    if len(updateBatch) > 0:
        result = bulkWrite(updateBatch, institutionCollection)
        bulkCounters = dict(Counter(bulkCounters) + Counter(result))

    print(f"{checkedInstitutionCount} / {totalInstitutionCount} - {bulkCounters}")
    client.close()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        pass
