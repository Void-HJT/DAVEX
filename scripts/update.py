import argparse
from ruamel.yaml import YAML

def update_yaml(file_path, name=None, ip=None, port=None, key_store_password=None, trust_store_password=None):
    yaml = YAML()
    yaml.preserve_quotes = True
    
    with open(file_path, 'r') as file:
        data = yaml.load(file)

    if name is not None:
        data['my']['name'] = name
    if ip is not None:
        data['my']['ip'] = ip
    if port is not None:
        data['server']['port'] = port
    if key_store_password is not None:
        data['ssl']['key-store-password'] = key_store_password
    if trust_store_password is not None:
        data['ssl']['trust-store-password'] = trust_store_password

    with open(file_path, 'w') as file:
        yaml.dump(data, file)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Update YAML file fields.')
    parser.add_argument('file', help='Path to the YAML file')
    parser.add_argument('--name', help='New value for my.name')
    parser.add_argument('--ip', help='New value for my.ip')
    parser.add_argument('--port', type=int, help='New value for server.port')
    parser.add_argument('--key_store_password', help='New value for ssl.key-store-password')
    parser.add_argument('--trust_store_password', help='New value for ssl.trust-store-password')

    args = parser.parse_args()
    update_yaml(args.file, args.name, args.ip, args.port, args.key_store_password, args.trust_store_password)
