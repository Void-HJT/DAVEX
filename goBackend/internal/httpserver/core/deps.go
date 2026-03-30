package core

import (
	"time"

	"goBackend/internal/chainmaker"
	"goBackend/internal/did"
	"goBackend/internal/ring_sign"
)

type RouterDeps struct {
	ChainMaker *chainmaker.Client
	DID        *did.Service
	Privacy    *ring_sign.PrivacyService
	Now        func() time.Time
}
