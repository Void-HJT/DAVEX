module=$1
name=$2
ip=$3
keyStore_password=$4
trustStore_password=$5

rm DAVEX_${module}/src/main/resources/jwt.jks
rm DAVEX_${module}/src/main/resources/truststore.jks
keytool -genkey -alias ${name} -keyalg RSA -keysize 2048 -validity 365 -keystore jwt.jks -dname "CN=${name}, OU=DAVEX, O=DAVEX, L=sh, ST=sh, C=CN" -ext "SAN=IP:${ip}" -storepass ${keyStore_password}
keytool -export -alias ${name} -file jwt.cer -keystore jwt.jks -storepass ${keyStore_password}
yes | keytool -import -alias ${name} -file jwt.cer -keystore truststore.jks -storepass ${trustStore_password}
rm jwt.cer
mv jwt.jks DAVEX_${module}/src/main/resources
mv truststore.jks DAVEX_${module}/src/main/resources
