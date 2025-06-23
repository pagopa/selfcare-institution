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

def checkInstitutionTypes(institutionDoc):
    institutionId = institutionDoc["_id"]
    if "onboarding" not in institutionDoc:
        print(AnsiColors.WARNING, f"Institution {institutionId} without onboarding node", AnsiColors.ENDC)
        return
    institutionType = institutionDoc["institutionType"] if "institutionType" in institutionDoc else None
    origin = institutionDoc["origin"] if "origin" in institutionDoc else None
    originId = institutionDoc["originId"] if "originId" in institutionDoc else None
    onboardingCounter = 0
    isValidInstitution = True
    for onboarding in institutionDoc["onboarding"]:
        onboardingInstitutionType = onboarding["institutionType"] if "institutionType" in onboarding else None
        onboardingOrigin = onboarding["origin"] if "origin" in onboarding else None
        onboardingOriginId = onboarding["originId"] if "originId" in onboarding else None
        diff = []
        if institutionType != onboardingInstitutionType:
            diff.append("institutionType")
        if origin != onboardingOrigin:
            diff.append("origin")
        if originId != onboardingOriginId:
            diff.append("originId")
        if diff:
            print(AnsiColors.ERROR, f"Institution {institutionId} has onboarding[{onboardingCounter}] with different {diff}", AnsiColors.ENDC)
            isValidInstitution = False
        onboardingCounter += 1
    return isValidInstitution

def main():
    client = MongoClient(MONGO_HOST)
    db = client[INSTITUTION_DB]
    institutionCollection = db[INSTITUTION_COLLECTION]

    totalInstitutionCount = institutionCollection.count_documents({})
    checkedInstitutionCount = 0
    validInstitutionCount = 0
    invalidInstitutionCount = 0
    skippedInstitutionCount = 0

    for institutionDoc in institutionCollection.find({}, batch_size=MONGO_BATCH_SIZE):
        check = checkInstitutionTypes(institutionDoc)
        if check is True:
            validInstitutionCount += 1
        elif check is False:
            invalidInstitutionCount += 1
        elif check is None:
            skippedInstitutionCount += 1
        checkedInstitutionCount += 1
        print(f"Checked {checkedInstitutionCount} / {totalInstitutionCount}", end="\r")

    print("\n")
    print(f"Valid Institutions: {validInstitutionCount} / {totalInstitutionCount}")
    print(f"Invalid Institutions: {invalidInstitutionCount} / {totalInstitutionCount}")
    print(f"Skipped Institutions (without onboarding node): {skippedInstitutionCount} / {totalInstitutionCount}")
    client.close()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        pass
