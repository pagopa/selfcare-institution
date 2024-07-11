prefix    = "selc"
env_short = "d"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Dev"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-institution"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 0
  max_replicas = 1
  scale_rules = [
    {
      custom = {
        metadata = {
          "desiredReplicas" = "1"
          "start"           = "0 8 * * MON-FRI"
          "end"             = "0 19 * * MON-FRI"
          "timezone"        = "Europe/Rome"
        }
        type = "cron"
      }
      name = "cron-scale-rule"
    }
  ]
  cpu    = 1
  memory = "2Gi"
}

app_settings = [
  {
    name  = "JAVA_TOOL_OPTIONS"
    value = "-javaagent:applicationinsights-agent.jar",
  },
  {
    name  = "APPLICATIONINSIGHTS_ROLE_NAME"
    value = "institution_send_mail_scheduler",
  },
  {
    name = "STORAGE_CONTAINER_CONTRACT"
    value = "selc-d-contracts-blob"
  },
  {
    name = "MAIL_DESTINATION_TEST_ADDRESS"
    value = "pectest@pec.pagopa.it"
  },
  {
    name = "MAIL_SERVER_HOST"
    value = "smtps.pec.aruba.it"
  },
  {
    name = "MAIL_SERVER_PORT"
    value = "465"
  },
  {
    name = "MAIL_TEMPLATE_NOTIFICATION_PATH"
    value = "contracts/template/mail/institution-user-list-notification/1.0.0.json"
  },
  {
    name = "MAIL_TEMPLATE_FIRST_NOTIFICATION_PATH"
    value = "contracts/template/mail/institution-user-list-first-notification/1.0.0.json"
  },
  {
    name = "STORAGE_CONTAINER_PRODUCT"
    value = "selc-d-product"
  },
  {
    name = "SELFCARE_USER_URL"
    value = "https://selc-d-user-ms-ca.politewater-9af33050.westeurope.azurecontainerapps.io"
  }
]


secrets_names = {
    "MONGODB-CONNECTION-STRING"               = "mongodb-connection-string"
    "BLOB-STORAGE-CONTRACT-CONNECTION-STRING" = "blob-storage-contract-connection-string"
    "BLOB_STORAGE_CONN_STRING_PRODUCT"        = "blob-storage-product-connection-string"
    "MAIL_SERVER_USERNAME"                    = "smtp-usr"
    "MAIL_SENDER_ADDRESS"                     = "smtp-usr"
    "MAIL_SERVER_PASSWORD"                    = "smtp-psw"
    "JWT_BEARER_TOKEN"                        = "jwt-bearer-token-functions"
}

