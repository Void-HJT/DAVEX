package handlers

import (
	"net/http"
)

const swaggerYAML = `openapi: 3.0.3
info:
  title: goBackend API
  version: 0.2.0
  description: DID + VC/VP + Ring Signature demo APIs (no cross-domain auth).
servers:
  - url: http://localhost:8081
paths:
  /api/v1/health:
    get:
      summary: Health check
      responses:
        "200":
          description: OK
  /api/v1/echo:
    post:
      summary: Echo JSON payload
      requestBody:
        required: true
        content:
          application/json:
            schema: {}
      responses:
        "200":
          description: OK
  /api/v1/did/generate:
    post:
      summary: Generate DID (local)
      responses:
        "200":
          description: OK
  /api/v1/did/register:
    post:
      summary: Register DID document on ChainMaker
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [didDocument]
              properties:
                didDocument:
                  type: object
      responses:
        "200":
          description: OK
  /api/v1/did/query:
    get:
      summary: Query DID registry from ChainMaker
      parameters:
        - in: query
          name: did
          required: true
          schema:
            type: string
      responses:
        "200":
          description: OK
  /api/v1/vc/issue:
    post:
      summary: Issue VC and write to chain
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              description: Provide vc directly OR provide issuerDid+issuer key+holderDid+credential fields.
              properties:
                vc:
                  type: object
                issuerDid:
                  type: string
                issuerPrivateKey:
                  type: string
                issuerPrivateKeyHex:
                  type: string
                issuerPublicKeyHex:
                  type: string
                holderDid:
                  type: string
                credentialType:
                  type: string
                credentialSubject:
                  type: object
      responses:
        "200":
          description: OK
  /api/v1/vc/verify:
    post:
      summary: Verify VC on chain
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/vp/generate:
    post:
      summary: Generate VP locally
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [holderDid, holderPrivateKeyHex, credentials, challenge]
              properties:
                holderDid:
                  type: string
                holderPrivateKeyHex:
                  type: string
                holderPublicKeyHex:
                  type: string
                credentials:
                  type: array
                  items:
                    type: object
                challenge:
                  type: string
                verifierDid:
                  type: string
      responses:
        "200":
          description: OK
  /api/v1/vp/verify:
    post:
      summary: Verify VP on chain
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/group/create:
    post:
      summary: Create ring-sign credential group
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/group/member:
    post:
      summary: Add group member public key
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/group:
    get:
      summary: Query group details
      parameters:
        - in: query
          name: groupId
          required: true
          schema:
            type: string
      responses:
        "200":
          description: OK
  /api/v1/privacy/vp/generate:
    post:
      summary: Generate privacy-preserving VP locally
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/vp/verify:
    post:
      summary: Verify privacy VP on chain
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/vp/verify-local:
    post:
      summary: Verify privacy VP locally
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/claims/build:
    post:
      summary: Build selective-disclosure claims from VC
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/keyimage:
    post:
      summary: Compute key image from private key
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
  /api/v1/privacy/verify-request/create:
    post:
      summary: Create verifier challenge request
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
      responses:
        "200":
          description: OK
`

func SwaggerYAMLHandler() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		w.Header().Set("Content-Type", "text/yaml; charset=utf-8")
		_, _ = w.Write([]byte(swaggerYAML))
	}
}

func SwaggerRedirectHandler() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		http.Redirect(w, r, "/swagger/", http.StatusMovedPermanently)
	}
}

func SwaggerUIHandler() http.HandlerFunc {
	const html = `<!doctype html>
<html>
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <title>goBackend Swagger</title>
    <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
  </head>
  <body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
    <script>
      window.onload = () => {
        SwaggerUIBundle({ url: '/swagger/swagger.yaml', dom_id: '#swagger-ui' });
      };
    </script>
  </body>
</html>`

	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			w.WriteHeader(http.StatusMethodNotAllowed)
			return
		}
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		_, _ = w.Write([]byte(html))
	}
}
