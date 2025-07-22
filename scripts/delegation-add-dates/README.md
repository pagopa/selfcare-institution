## Add createdAt in delegation script

- Python script to add createdAt field in Delegations when it doesn't exist.
- Python script to check if all dates are with a valid format.

## Requirements

- Python 3.6 or higher
- MongoDB
- Required Python packages: `python-dotenv`, `pymongo`, `python-dateutil`

## Steps

1. Connects to the MongoDB database. (local, dev, uat, prod)
2. Set MONGO_HOST environment variable
3. Run `pyhton delegation_add_createdAt.py` in selfcare-institution/scripts/delegation-add-createdAt
4. Run `pyhton check_valid_dates.py` in selfcare-institution/scripts/delegation-add-createdAt


