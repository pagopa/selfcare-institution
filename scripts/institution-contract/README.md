## Contract update script

Python script to update contract field in Institution collection.

## Requirements

- Python 3.6 or higher
- MongoDB
- Required Python packages: `python-dotenv`, `pymongo`, `python-dateutil`

## Setup

1. **Clone the repository:**
   ```sh
   git clone <repository_url>
   cd <repository_directory>/selfcare-institution/scripts/institution-contract
   ```

## Steps

1. Connects to the MongoDB database. (local, dev, uat, prod)
2. Create a .env file in the root directory of the project and add the environment variables inside .env.example
3. Run `pyhton update_contract.py` in selfcare-institution/scripts/institution-contract

