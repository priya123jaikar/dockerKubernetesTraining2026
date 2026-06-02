variable "bucket_name"{
    description = "The name of the S3 bucket"
    type        = string
    default     = "my-terraform-s3-bucket"
}

variable "region" {
    description = "The AWS region where the resources will be created"
    type        = string
    default     = "us-east-1"
}

variable "environment" {
    description = "The environment for the resources (e.g., dev, staging, prod)"
    type        = string
    default     = "dev"
}

variable "cidr_block" {
    description = "The CIDR block for the VPC"
    type        = string
}

variable "subnet_cidr_block" {
    description = "The CIDR block for the subnet"
    type        = string
}