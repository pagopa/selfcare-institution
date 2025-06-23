#!/usr/bin/env python3

import os

from pymongo import MongoClient

MONGO_HOST = os.getenv("MONGO_HOST")
MONGO_BATCH_SIZE = 100
INSTITUTION_DB = "selcMsCore"
INSTITUTION_COLLECTION = "Institution"

class AnsiColors:
    WARNING = '\033[93m'
    ERROR = '\033[91m'
    ENDC = '\033[0m'

def updateInstitutionTypes(institutionDoc, institutionCollection):
    institutionId = institutionDoc["_id"]
    if "onboarding" not in institutionDoc:
        print(AnsiColors.WARNING, f"Institution {institutionId} without onboarding node", AnsiColors.ENDC)
        return
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
    # final update
    institutionCollection.update_one({"_id": institutionId}, {"$set": fieldsToSet, "$unset": fieldsToUnset})

def main():
    client = MongoClient(MONGO_HOST)
    db = client[INSTITUTION_DB]
    institutionCollection = db[INSTITUTION_COLLECTION]

    totalInstitutionCount = institutionCollection.count_documents({})
    updatedInstitutionCount = 0

    for institutionDoc in institutionCollection.find({}, batch_size=MONGO_BATCH_SIZE):
        updateInstitutionTypes(institutionDoc, institutionCollection)
        updatedInstitutionCount += 1
        print(f"Updated {updatedInstitutionCount} / {totalInstitutionCount}", end="\r")

    print("\n")
    client.close()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        pass
