# income-tax-self-employment

This is where we make API calls from users viewing and making changes to the Self-Employment section of their income tax
return.

## Running the service locally

You will need to have the following:

- Installed/configured [service manager](https://github.com/hmrc/service-manager).

The service manager profile for this service is:

    sm --start INCOME_TAX_SELF_EMPLOYMENT

Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm --start INCOME_TAX_SUBMISSION_ALL -r

This service runs on port: `localhost:10900`

## API Code Generation

### Prerequisites

- `openapi-generator` CLI tool installed.
- YAML API swagger file.

### Steps

1. **Download the YAML Swagger file from confluence page**

2. **Generate Scala Code**:
   Use the `openapi-generator` tool to generate the Scala code:

```bash
   openapi-generator generate -i api-1894.yaml -g scala-play-server -o output
```

3. **Copy case classes**

Navigate to the `output/app/model` directory and copy the generated case classes to your project. To be on safe side
just File Explorer, as Intellij likes to cut some of the code.

4. **Remove annotations and rename package**

Remove the `javax` annotations from the generated case classes. Rename package name.

5. **Add Copyright**

Add copyright. You can use Intellij copyright profile and Action: `Update Copyright`.

6. **Tweak it as you need it**

Sometimes generator is confused with large yaml file, so manual intervention is required. Fix it manually and add it
below
if this is a new tweak.

### Known codegen issues

#### The file is cut

Copying between intellij may cause some files cut, it's better to use File Explorer.

#### The additionalProperties field is generated but there is no type next to it

There was situation with API#1802 which caused strange additionalProperties: <empty> to be generated, even though
it is false in yaml. We had to remove it manually.

#### JsObject does not compile

There was a companion object custom Format with this line which was not
compiling: `val newObj = JsObject(restructuredProps)`.

The solution was to just use our macro like in other case classes: `Json.format[AnnualAllowancesType]`. This approach
should work when additionalProperties is false.

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").