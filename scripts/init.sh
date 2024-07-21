name=$1
ip=$2
port=$3
keyStore_password=$4
trustStore_password=$5


./scripts/ssl.sh $name $ip $keyStore_password $trustStore_password
pip install ruamel.yaml
python3 ./scripts/update.py DVE_agent/backend/src/main/resources/application.yml --name $name --ip $ip --port $port --key_store_password $keyStore_password --trust_store_password $trustStore_password