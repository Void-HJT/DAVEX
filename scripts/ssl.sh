name=$1
ip=$2
keyStore_password=$3
trustStore_password=$4

rm DVE_agent/backend/src/main/resources/jwt.jks
rm DVE_agent/backend/src/main/resources/truststore.jks
keytool -genkey -alias ${name} -keyalg RSA -keysize 2048 -validity 365 -keystore jwt.jks -dname "CN=${name}, OU=DVE, O=DVE, L=sh, ST=sh, C=CN" -ext "SAN=IP:${ip}" -storepass ${keyStore_password}
keytool -export -alias ${name} -file jwt.cer -keystore jwt.jks -storepass ${keyStore_password}
yes | keytool -import -alias ${name} -file jwt.cer -keystore truststore.jks -storepass ${trustStore_password}
rm jwt.cer
mv jwt.jks DVE_agent/backend/src/main/resources
mv truststore.jks DVE_agent/backend/src/main/resources
