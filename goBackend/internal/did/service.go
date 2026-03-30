package did

import (
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"math/big"
	"time"
)

type Service struct{}

func NewService() *Service {
	return &Service{}
}

type KeyPair struct {
	PrivateKey   *ecdsa.PrivateKey
	PublicKey    *ecdsa.PublicKey
	Address      string
	PublicKeyHex string
}

func (s *Service) GenerateKeyPair() (*KeyPair, error) {
	privateKey, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		return nil, fmt.Errorf("generate key pair failed: %v", err)
	}

	publicKeyBytes := elliptic.Marshal(
		privateKey.PublicKey.Curve,
		privateKey.PublicKey.X,
		privateKey.PublicKey.Y,
	)
	publicKeyHex := "0x" + hex.EncodeToString(publicKeyBytes)

	hash := sha256.Sum256(publicKeyBytes)
	address := "0x" + hex.EncodeToString(hash[:20])

	return &KeyPair{
		PrivateKey:   privateKey,
		PublicKey:    &privateKey.PublicKey,
		Address:      address,
		PublicKeyHex: publicKeyHex,
	}, nil
}

func (s *Service) RecoverKeyPairFromHex(privateKeyHex, publicKeyHex string) (*KeyPair, error) {
	privateKeyHex = normalizeHexForDecode(privateKeyHex)
	publicKeyHex = normalizeHexForDecode(publicKeyHex)

	privateKeyBytes, err := hex.DecodeString(privateKeyHex)
	if err != nil {
		return nil, fmt.Errorf("decode private key failed: %v", err)
	}
	publicKeyBytes, err := hex.DecodeString(publicKeyHex)
	if err != nil {
		return nil, fmt.Errorf("decode public key failed: %v", err)
	}

	curve := elliptic.P256()
	x, y := elliptic.Unmarshal(curve, publicKeyBytes)
	if x == nil {
		return nil, fmt.Errorf("invalid public key format")
	}

	privateKey := &ecdsa.PrivateKey{
		PublicKey: ecdsa.PublicKey{
			Curve: curve,
			X:     x,
			Y:     y,
		},
		D: new(big.Int).SetBytes(privateKeyBytes),
	}

	hash := sha256.Sum256(publicKeyBytes)
	address := "0x" + hex.EncodeToString(hash[:20])

	return &KeyPair{
		PrivateKey:   privateKey,
		PublicKey:    &privateKey.PublicKey,
		Address:      address,
		PublicKeyHex: "0x" + publicKeyHex,
	}, nil
}

func (s *Service) RecoverKeyPairFromPrivateHex(privateKeyHex string) (*KeyPair, error) {
	privateKeyHex = normalizeHexForDecode(privateKeyHex)
	privateKeyBytes, err := hex.DecodeString(privateKeyHex)
	if err != nil {
		return nil, fmt.Errorf("decode private key failed: %v", err)
	}

	curve := elliptic.P256()
	privateKey := new(ecdsa.PrivateKey)
	privateKey.D = new(big.Int).SetBytes(privateKeyBytes)
	privateKey.PublicKey.Curve = curve
	privateKey.PublicKey.X, privateKey.PublicKey.Y = curve.ScalarBaseMult(privateKey.D.Bytes())

	publicKeyBytes := elliptic.Marshal(curve, privateKey.PublicKey.X, privateKey.PublicKey.Y)
	hash := sha256.Sum256(publicKeyBytes)
	address := "0x" + hex.EncodeToString(hash[:20])

	return &KeyPair{
		PrivateKey:   privateKey,
		PublicKey:    &privateKey.PublicKey,
		Address:      address,
		PublicKeyHex: "0x" + hex.EncodeToString(publicKeyBytes),
	}, nil
}

func (s *Service) GenerateDIDDocument(keyPair *KeyPair) (*DIDDocument, error) {
	if keyPair == nil || keyPair.PrivateKey == nil {
		return nil, fmt.Errorf("keyPair is nil")
	}

	did := "did:mychain:" + keyPair.Address[2:]

	doc := &DIDDocument{
		ID:         did,
		Controller: did,
		VerificationMethod: []VerificationMethod{
			{
				ID:           did + "#key-1",
				Type:         "EcdsaSecp256k1VerificationKey2019",
				Controller:   did,
				PublicKeyHex: keyPair.PublicKeyHex,
				Address:      keyPair.Address,
			},
		},
		Authentication: []string{did + "#key-1"},
		Created:        time.Now().Format(time.RFC3339),
	}

	proof, err := s.SignDIDDocument(doc, keyPair)
	if err != nil {
		return nil, err
	}
	doc.Proof = proof

	return doc, nil
}

func (s *Service) SignDIDDocument(doc *DIDDocument, keyPair *KeyPair) (*Proof, error) {
	if doc == nil || keyPair == nil || keyPair.PrivateKey == nil {
		return nil, fmt.Errorf("invalid input")
	}

	docCopy := *doc
	docCopy.Proof = nil

	hash, err := s.calculateDocumentHash(&docCopy)
	if err != nil {
		return nil, err
	}

	r, ss, err := ecdsa.Sign(rand.Reader, keyPair.PrivateKey, hash)
	if err != nil {
		return nil, fmt.Errorf("sign failed: %w", err)
	}

	signature := append(leftPad32(r), leftPad32(ss)...)
	signatureHex := "0x" + hex.EncodeToString(signature)

	return &Proof{
		Type:               "EcdsaSecp256k1Signature2019",
		Created:            time.Now().Format(time.RFC3339),
		VerificationMethod: doc.ID + "#key-1",
		ProofPurpose:       "authentication",
		ProofValue:         signatureHex,
	}, nil
}

func (s *Service) GenerateVC(
	issuerDID string,
	issuerKeyPair *KeyPair,
	holderDID string,
	credentialType string,
	credentialSubject map[string]interface{},
) (*VerifiableCredential, error) {
	if issuerDID == "" {
		return nil, fmt.Errorf("issuerDID is required")
	}
	if issuerKeyPair == nil || issuerKeyPair.PrivateKey == nil {
		return nil, fmt.Errorf("issuer key pair is nil")
	}
	if holderDID == "" {
		return nil, fmt.Errorf("holderDID is required")
	}
	if credentialType == "" {
		return nil, fmt.Errorf("credentialType is required")
	}
	if credentialSubject == nil {
		credentialSubject = map[string]interface{}{}
	}

	if existingHolder, ok := credentialSubject["id"].(string); ok && existingHolder != "" && existingHolder != holderDID {
		return nil, fmt.Errorf("credentialSubject.id mismatch: %s != %s", existingHolder, holderDID)
	}
	credentialSubject["id"] = holderDID

	vc := &VerifiableCredential{
		ID:                "vc:credential:" + generateUUID(),
		Type:              []string{"VerifiableCredential", credentialType},
		Issuer:            issuerDID,
		IssuanceDate:      time.Now().Format(time.RFC3339),
		ExpirationDate:    time.Now().AddDate(1, 0, 0).Format(time.RFC3339),
		CredentialSubject: credentialSubject,
	}

	proof, err := s.SignVC(vc, issuerKeyPair, issuerDID)
	if err != nil {
		return nil, fmt.Errorf("sign VC failed: %v", err)
	}
	vc.Proof = proof

	return vc, nil
}

func (s *Service) SignVC(vc *VerifiableCredential, keyPair *KeyPair, issuerDID string) (*Proof, error) {
	vcCopy := *vc
	vcCopy.Proof = nil

	hash, err := s.calculateVCHash(&vcCopy)
	if err != nil {
		return nil, err
	}

	r, ss, err := ecdsa.Sign(rand.Reader, keyPair.PrivateKey, hash)
	if err != nil {
		return nil, fmt.Errorf("sign failed: %v", err)
	}
	signature := append(leftPad32(r), leftPad32(ss)...)

	return &Proof{
		Type:               "EcdsaSecp256k1Signature2019",
		Created:            time.Now().Format(time.RFC3339),
		VerificationMethod: issuerDID + "#key-1",
		ProofPurpose:       "assertionMethod",
		ProofValue:         "0x" + hex.EncodeToString(signature),
	}, nil
}

func (s *Service) GenerateVP(
	holderDID string,
	holderKeyPair *KeyPair,
	credentials []VerifiableCredential,
	challenge string,
	verifierDID string,
) (*VerifiablePresentation, error) {
	if holderDID == "" {
		return nil, fmt.Errorf("holderDID is required")
	}
	if holderKeyPair == nil || holderKeyPair.PrivateKey == nil {
		return nil, fmt.Errorf("holder key pair is nil")
	}
	if challenge == "" {
		return nil, fmt.Errorf("challenge is required")
	}
	if len(credentials) == 0 {
		return nil, fmt.Errorf("credentials are required")
	}
	for i, vc := range credentials {
		if id, ok := vc.CredentialSubject["id"].(string); ok && id != "" && id != holderDID {
			return nil, fmt.Errorf("credential %d holder mismatch: %s != %s", i, id, holderDID)
		}
	}

	rawVCs, err := VCListToRawMessages(credentials)
	if err != nil {
		return nil, fmt.Errorf("marshal VC list failed: %v", err)
	}

	vp := &VerifiablePresentation{
		ID:                   "vp:presentation:" + generateUUID(),
		Type:                 "VerifiablePresentation",
		Holder:               holderDID,
		VerifiableCredential: rawVCs,
		Challenge:            challenge,
		Verifier:             verifierDID,
		Created:              time.Now().Format(time.RFC3339),
	}

	proof, err := s.SignVP(vp, holderKeyPair, holderDID, challenge)
	if err != nil {
		return nil, fmt.Errorf("sign VP failed: %v", err)
	}
	vp.Proof = proof

	return vp, nil
}

func (s *Service) SignVP(vp *VerifiablePresentation, keyPair *KeyPair, holderDID, challenge string) (*Proof, error) {
	vpCopy := *vp
	vpCopy.Proof = nil

	hash, err := s.calculateVPHash(&vpCopy)
	if err != nil {
		return nil, err
	}

	r, ss, err := ecdsa.Sign(rand.Reader, keyPair.PrivateKey, hash)
	if err != nil {
		return nil, fmt.Errorf("sign failed: %v", err)
	}
	signature := append(leftPad32(r), leftPad32(ss)...)

	return &Proof{
		Type:               "EcdsaSecp256k1Signature2019",
		Created:            time.Now().Format(time.RFC3339),
		VerificationMethod: holderDID + "#key-1",
		ProofPurpose:       "authentication",
		ProofValue:         "0x" + hex.EncodeToString(signature),
		Challenge:          challenge,
	}, nil
}

func (s *Service) calculateDocumentHash(doc *DIDDocument) ([]byte, error) {
	data, err := doc.ToJSON()
	if err != nil {
		return nil, err
	}
	sum := sha256.Sum256([]byte(data))
	return sum[:], nil
}

func (s *Service) calculateVCHash(vc *VerifiableCredential) ([]byte, error) {
	data, err := vc.ToJSON()
	if err != nil {
		return nil, err
	}
	sum := sha256.Sum256([]byte(data))
	return sum[:], nil
}

func (s *Service) calculateVPHash(vp *VerifiablePresentation) ([]byte, error) {
	data, err := vp.ToJSON()
	if err != nil {
		return nil, err
	}
	sum := sha256.Sum256([]byte(data))
	return sum[:], nil
}

func leftPad32(v *big.Int) []byte {
	if v == nil {
		return make([]byte, 32)
	}
	b := v.Bytes()
	if len(b) >= 32 {
		return b[len(b)-32:]
	}
	out := make([]byte, 32)
	copy(out[32-len(b):], b)
	return out
}

func trimHexPrefix(v string) string {
	if len(v) >= 2 && (v[:2] == "0x" || v[:2] == "0X") {
		return v[2:]
	}
	return v
}

func normalizeHexForDecode(v string) string {
	v = trimHexPrefix(v)
	if len(v)%2 == 1 {
		v = "0" + v
	}
	return v
}

func generateUUID() string {
	b := make([]byte, 16)
	_, _ = rand.Read(b)
	return fmt.Sprintf("%x-%x-%x-%x-%x", b[0:4], b[4:6], b[6:8], b[8:10], b[10:])
}

func VCListToRawMessages(credentials []VerifiableCredential) ([]json.RawMessage, error) {
	rawVCs := make([]json.RawMessage, 0, len(credentials))
	for i, vc := range credentials {
		b, err := json.Marshal(vc)
		if err != nil {
			return nil, fmt.Errorf("marshal VC %d failed: %v", i, err)
		}
		rawVCs = append(rawVCs, json.RawMessage(b))
	}
	return rawVCs, nil
}

func DIDFromAddress(address string) string {
	if address == "" {
		return ""
	}
	address = trimHexPrefix(address)
	return "did:mychain:" + address
}
