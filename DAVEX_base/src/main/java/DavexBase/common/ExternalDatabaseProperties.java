package DavexBase.common;


import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "external-databases")
public class ExternalDatabaseProperties {

    private List<DatabaseConfig> databases;

    @XmlElement(name = "database")
    public List<DatabaseConfig> getDatabases() {
        return databases;
    }

    public void setDatabases(List<DatabaseConfig> databases) {
        this.databases = databases;
    }

    public static class DatabaseConfig {
        private String name;
        private String type;
        private String url;
        private String username;
        private String password;

        // Getters and Setters
        @XmlElement
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @XmlElement
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @XmlElement
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @XmlElement
        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}