prefix    = "selc"
env_short = "p"

tags = {
  CreatedBy   = "Terraform"
  Environment = "Prod"
  Owner       = "SelfCare"
  Source      = "https://github.com/pagopa/selfcare-institution"
  CostCenter  = "TS310 - PAGAMENTI & SERVIZI"
}

container_app = {
  min_replicas = 1
  max_replicas = 1
  scale_rules  = []
  cpu          = 1
  memory       = "2Gi"
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
    value = "selc-p-contracts-blob"
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
    value = "contracts/template/mail/user-list-notification/1.0.0.json"
  },
  {
    name = "STORAGE_CONTAINER_PRODUCT"
    value = "selc-p-product"
  },
  {
    name = "MAIL_DESTINATION_TEST"
    value = "false"
  }
]


secrets_names = {
  "MONGODB-CONNECTION-STRING"               = "mongodb-connection-string"
  "BLOB-STORAGE-CONTRACT-CONNECTION-STRING" = "blob-storage-contract-connection-string"
  "BLOB_STORAGE_CONN_STRING_PRODUCT"        = "blob-storage-product-connection-string"
  "MAIL_SERVER_USERNAME"                    = "smtp-usr"
  "MAIL_SENDER_ADDRESS"                     = "smtp-usr"
  "MAIL_SERVER_PASSWORD"                    = "smtp-psw"

}

