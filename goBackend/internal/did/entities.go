package did

import "encoding/json"

type DIDDocument struct {
	ID                 string               `json:"id"`
	Controller         string               `json:"controller"`
	VerificationMethod []VerificationMethod `json:"verificationMethod"`
	Authentication     []string             `json:"authentication"`
	Created            string               `json:"created"`
	Updated            string               `json:"updated,omitempty"`
	Proof              *Proof               `json:"proof"`
}

type VerifiableCredential struct {
	ID                string                 `json:"id"`
	Type              []string               `json:"type"`
	Issuer            string                 `json:"issuer"`
	IssuanceDate      string                 `json:"issuanceDate"`
	ExpirationDate    string                 `json:"expirationDate"`
	CredentialSubject map[string]interface{} `json:"credentialSubject"`
	CredentialStatus  *CredentialStatus      `json:"credentialStatus,omitempty"`
	Proof             *Proof                 `json:"proof"`
}

type VerifiablePresentation struct {
	ID                   string            `json:"id"`
	Type                 string            `json:"type"`
	Holder               string            `json:"holder"`
	VerifiableCredential []json.RawMessage `json:"verifiableCredential"`
	Challenge            string            `json:"challenge"`
	Domain               string            `json:"domain,omitempty"`
	Verifier             string            `json:"verifier,omitempty"`
	Created              string            `json:"created"`
	Proof                *Proof            `json:"proof"`
}

type Proof struct {
	Type               string `json:"type"`
	Created            string `json:"created"`
	VerificationMethod string `json:"verificationMethod"`
	ProofPurpose       string `json:"proofPurpose"`
	ProofValue         string `json:"proofValue"`
	Challenge          string `json:"challenge,omitempty"`
	Domain             string `json:"domain,omitempty"`
}

type VerificationMethod struct {
	ID           string `json:"id"`
	Type         string `json:"type"`
	Controller   string `json:"controller"`
	PublicKeyHex string `json:"publicKeyHex"`
	Address      string `json:"address"`
}

type CredentialStatus struct {
	ID            string `json:"id"`
	Type          string `json:"type"`
	StatusPurpose string `json:"statusPurpose"`
}

func (d *DIDDocument) ToJSON() (string, error) {
	b, err := json.Marshal(d)
	if err != nil {
		return "", err
	}
	return string(b), nil
}

func (vc *VerifiableCredential) ToJSON() (string, error) {
	b, err := json.Marshal(vc)
	if err != nil {
		return "", err
	}
	return string(b), nil
}

func (vp *VerifiablePresentation) ToJSON() (string, error) {
	b, err := json.Marshal(vp)
	if err != nil {
		return "", err
	}
	return string(b), nil
}
