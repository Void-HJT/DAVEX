subj=$1
days=$2
name=$3
password=$4

openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl req -new -x509 -key private_key.pem -out certificate.pem -days ${days} -subj ${subj}
openssl pkcs12 -export -in certificate.pem -inkey private_key.pem -out DVE_agent/backend/src/main/resources/keystore.p12 -name ${name} -passout pass:${password}
rm private_key.pem certificate.pem
