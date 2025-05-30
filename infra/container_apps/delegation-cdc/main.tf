terraform {
  required_version = ">= 1.6.0"

  backend "azurerm" {}
}

provider "azurerm" {
  features {}
  skip_provider_registration = true

}

module "container_app_delegation_cdc" {
  source = "github.com/pagopa/selfcare-commons//infra/terraform-modules/container_app_microservice?ref=main"

  is_pnpg = var.is_pnpg

  env_short                      = var.env_short
  resource_group_name            = local.ca_resource_group_name
  container_app                  = var.container_app
  container_app_name             = "delegation-cdc"
  container_app_environment_name = local.container_app_environment_name
  image_name                     = "selfcare-delegation-cdc"
  image_tag                      = var.image_tag
  app_settings                   = var.app_settings
  secrets_names                  = var.secrets_names
  workload_profile_name          = var.workload_profile_name

  probes = [
    {
      httpGet = {
        path   = "q/health/live"
        port   = 8080
        scheme = "HTTP"
      }
      timeoutSeconds      = 5
      type                = "Liveness"
      failureThreshold    = 3
      initialDelaySeconds = 1
    },
    {
      httpGet = {
        path   = "q/health/ready"
        port   = 8080
        scheme = "HTTP"
      }
      timeoutSeconds      = 5
      type                = "Readiness"
      failureThreshold    = 30
      initialDelaySeconds = 3
    },
    {
      httpGet = {
        path   = "q/health/started"
        port   = 8080
        scheme = "HTTP"
      }
      timeoutSeconds      = 5
      failureThreshold    = 5
      type                = "Startup"
      initialDelaySeconds = 5
    }
  ]

  tags = var.tags
}
