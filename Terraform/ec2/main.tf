resource "aws_instance" "ec2instance" {
  depends_on   = [aws_s3_bucket.s3bucket, aws_subnet.subnet]
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t2.micro"
  subnet_id     = aws_subnet.subnet.id
  tags          = aws_s3_bucket.s3bucket.tags
}