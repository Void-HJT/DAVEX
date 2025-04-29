package DavexBase.service.TEE.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class RegisterDataKeys {
    private List<DataKey> data_keys = new ArrayList<>();
}