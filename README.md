# FOLIO - MARC Order Import
Proof of concept workaround needed until FOLIO supports importing MARC records to create orders.

> Affectionately referred to as the _Lehigh App_ here at Cornell
## Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
  > If using Docker Desktop, [ensure filesystem sharing for macOS](https://docs.docker.com/desktop/mac/#file-sharing) or [shared drives for Windows](https://docs.docker.com/desktop/windows/#file-sharing) is configured properly to support mounting files at the path of your working copy for this repo.
- [Docker Compose](https://docs.docker.com/compose/install/)
- FOLIO user account with necessary permissions
  > see [folio-permset.json](folio-permset.json)

## What does it do?
* It takes an uploaded file that contains MARC records and creates orders, instances, holdings, items and MARC records for each.
* It uses the 980 field to get the fund code, vendor code, price, tag, quantity, notes and electronic/print indicator
* It only creates items if the material is print (which should be indicated in the 980$z field) - It looks for the values ELECTRONIC or PRINT
* It lets FOLIO create the instance, holdings and item records using the "createInventory" value in the order line
* It does all of this using the FOLIO API
* It uses a property file to determine location, fiscal year, loan type, note type and material type and default text for electronic resources (in case subfield z is missing)

## API Calls
* Several get calls to initialize reference values (like instance types, material types, note types)
* Get next PO number (GET orders/po-number)
* Posts a purchase order (approved and open) and one line item for each MARC record in a file (POST orders/composite-orders)
* Retreives the puchase order (to get the ID of the instance FOLIO automatically created) (GET orders/composite-orders/theOrderUuid)
* Retreive the new instance (GET inventory/instances/theinstanceid)
* Posts to copycat (POST copycat/imports)

## Dev Quickstart

1. Clone this repo

   ```sh
   git clone git@github.com:cul-it/order-import-poc.git
   cd order-import-poc
   ```

1. Setup environment variables

   ```sh
   cp .env.example .env
   ```
   > Replace **CHANGEME** placeholders in `.env` with appropriate values

1. Build the Docker Image

   ```sh
   docker-compose build
   ```

1. Start the Dev Environment

    ```sh
    docker-compose up
    ```

Once the dev environment has started, the app should be running at http://localhost:8080/order-import-poc/import.

### Stopping the Dev Environment

```
docker-compose down
```
> If running in the foreground, simply press `CTRL + C`

## Lots of areas for improvement including:
* Better way to get data out of the MARC record to include on the instance. 
* Better way to store reference values needed for lookup
* Current version contains some hard-coded values (e.g. currency: USD)
* If duplicate PO number error - get the next PO number and try again

## What's new?
* 1-15-2021
  - Add Docker Support: See [Docker.md](Docker.md)

* 11-16-2020
  - Removed reference to the 001 field.  Wasn't necessary and was causing an error when it was missing.
 
* 9-23-2020
  - Removed the check for enough money in the budget
  - Fixed where the electronic indicator is initialized (needed to be per record)
  
* 7-31-2020
  - Better handling of special characters
  - Handles multiple records in a file
  - Validates each record in the file 
