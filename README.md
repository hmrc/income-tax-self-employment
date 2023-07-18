
# income-tax-self-employment

This is where we make API calls from users viewing and making changes to the Self-Employment section of their income tax return.

## Running the service locally

You will need to have the following:
- Installed/configured [service manager](https://github.com/hmrc/service-manager).

The service manager profile for this service is:

    sm --start INCOME_TAX_SELF_EMPLOYMENT
Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm --start INCOME_TAX_SUBMISSION_ALL -r

This service runs on port: `localhost:10900`


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").