package main

import (
	"flag"
	"fmt"
	"log"
	"runtime"
	"time"
	"unsafe"

	"github.com/panjf2000/gnet"
)

type request struct {
	proto, method string
	path, query   string
	head, body    string
	remoteAddr    string
}

type httpServer struct {
	*gnet.EventServer
}

type httpCodec struct {
	req request
}

func (hc *httpCodec) Encode(c gnet.Conn, buf []byte) (out []byte, err error) {
	return buf, nil
}

func (hc *httpCodec) Decode(c gnet.Conn) (out []byte, err error) {
	buf := c.Read()
	c.ResetBuffer()

	// process the pipeline
	var leftover []byte
pipeline:
	if leftover, _ = parseReq(buf, &hc.req); len(leftover) == len(buf) {
		// request not ready, yet
		return
	}
	out = appendResp(out)
	buf = leftover
	goto pipeline
}

func (hs *httpServer) OnInitComplete(srv gnet.Server) (action gnet.Action) {
	log.Printf("HTTP server is listening on %s (multi-cores: %t, loops: %d)\n",
		srv.Addr.String(), srv.Multicore, srv.NumEventLoop)
	return
}

func (hs *httpServer) React(frame []byte, c gnet.Conn) (out []byte, action gnet.Action) {
	// handle the request
	out = frame
	return
}

func init() {
	runtime.GOMAXPROCS(runtime.NumCPU() * 2)
}

func main() {
	var port int
	var multicore bool

	// Example command: go run main.go --port 8080 --multicore=true
	flag.IntVar(&port, "port", 8080, "server port")
	flag.BoolVar(&multicore, "multicore", true, "multicore")
	flag.Parse()

	http := new(httpServer)
	hc := new(httpCodec)

	// Start serving!
	log.Fatal(gnet.Serve(http, fmt.Sprintf("tcp://:%d", port), gnet.WithMulticore(multicore), gnet.WithCodec(hc)))
}

// appendResp will append a valid http response to the provide bytes.
// The status param should be the code plus text such as "200 OK".
// The head parameter should be a series of lines ending with "\r\n" or empty.
func appendResp(b []byte) []byte {
	b = append(b, "HTTP/1.1 200 OK\r\nServer: gnet\r\nContent-Type: text/plain\r\nDate: "...)

	b = time.Now().AppendFormat(b, "Mon, 02 Jan 2006 15:04:05 GMT")

	b = append(b, "\r\nContent-Length: 13\r\n\r\nHello, World!"...)

	return b
}

func b2s(b []byte) string {
	return *(*string)(unsafe.Pointer(&b))
}

// parseReq is a very simple http request parser. This operation
// waits for the entire payload to be buffered before returning a
// valid request.
func parseReq(data []byte, req *request) (leftover []byte, err error) {
	sdata := b2s(data)
	dlen := len(sdata)
	var (
		i, s int
		head string
		q    = -1
	)
	// method, path, proto line
	for ; i < dlen; i++ {
		if sdata[i] == ' ' {
			req.method = sdata[s:i]
			for i, s = i+1, i+1; i < dlen; i++ {
				if sdata[i] == '?' && q == -1 {
					q = i - s
				} else if sdata[i] == ' ' {
					if q != -1 {
						req.path = sdata[s:q]
						req.query = req.path[q+1 : i]
					} else {
						req.path = sdata[s:i]
					}
					for i, s = i+1, i+1; i < dlen; i++ {
						if sdata[i] == '\n' && sdata[i-1] == '\r' {
							req.proto = sdata[s:i]
							i, s = i+1, i+1
							break
						}
					}
					break
				}
			}
			break
		}
	}
	if req.proto == "" {
		return data, fmt.Errorf("malformed request")
	}
	head = sdata[:s]
	for ; i < dlen; i++ {
		if i > 1 && sdata[i] == '\n' && sdata[i-1] == '\r' {
			line := sdata[s : i-1]
			s = i + 1
			if line == "" {
				req.head = sdata[len(head)+2 : i+1]
				i++
				return data[i:], nil
			}
		}
	}
	// not enough data
	return data, nil
}
