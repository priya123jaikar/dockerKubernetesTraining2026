variable "repository_name" {
  description = "The name of the ECR repository."
  type        = string
}

variable "region" {
  description = "The AWS region where the ECR repository will be created."
  type        = string
}

variable "environment" {
  description = "The environment for which the ECR repository is being created."
  type        = string
}