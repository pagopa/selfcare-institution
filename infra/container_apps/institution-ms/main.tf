terraform {
  required_version = ">= 1.6.0"

  backend "azurerm" {}
}

provider "azurerm" {
  features {}
}

module "container_app_core" {
  source = "github.com/pagopa/selfcare-commons//infra/terraform-modules/container_app_microservice?ref=main"

  is_pnpg = var.is_pnpg

  env_short                      = var.env_short
  container_app                  = var.container_app
  resource_group_name            = local.ca_resource_group_name
  container_app_name             = "ms-core"
  container_app_environment_name = local.container_app_environment_name
  image_name                     = "selfcare-institution-ms"
  image_tag                      = var.image_tag
  app_settings                   = var.app_settings
  secrets_names                  = var.secrets_names
  workload_profile_name          = var.workload_profile_name

  tags = var.tags
}
