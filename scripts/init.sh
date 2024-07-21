module=$1
name=$2
ip=$3
port=$4
keyStore_password=$5
trustStore_password=$6


./scripts/ssl.sh ${module} $name $ip $keyStore_password $trustStore_password
pip install ruamel.yaml
python3 ./scripts/update.py DVE_${module}/src/main/resources/application.yml --name $name --ip $ip --port $port --key_store_password $keyStore_password --trust_store_password $trustStore_password