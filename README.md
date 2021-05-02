
## 1 简要概述
最近看起go lang，真的被go的goroutine（协程）惊艳到了，一句 go function(){#todo}，即可完成一个并发的工作。

看到gin这个web框架时，突然就特别想拿它和springboot来做个性能对比，马上撸一遍。

请求：/ping

返回：{"message":"pong"}

先透露下对比报告：


|   | qps | CPU | 内存 | 包大小
|---|---|---|---|---
|gin| 14900 | 150% | 0.4% | 9M 
|springboot|11536| 143% | 12% | 24M 

## 2 环境准备
- 2台2C4G的云主机（172.16.60.211，172.16.60.210），这个自己到阿里云上购买即可。一小时0.8元左右。
- gin的helloworld代码：https://github.com/qinxiongzhou/gin-vs-springboot/tree/main/springboot
- springboot的helloworld代码：https://github.com/qinxiongzhou/gin-vs-springboot/tree/main/gin/src/http_gin
- 172.16.60.211机器上，上次gin和springboot编译好的包，并启动运行。gin运行在8080端口，springboot运行在8090端口
- 172.16.60.210机器上，安装AB 工具包，做压测控制

## 3 代码工程及打包

### 3.1 gin

关键代码：

```go
func main() {
	gin.SetMode(gin.ReleaseMode)
	gin.DefaultWriter = ioutil.Discard
	r := gin.Default()
	r.GET("/ping", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "pong",
		})
	})
	r.Run() // listen and serve on 0.0.0.0:8080 (for windows "localhost:8080")
}
```

打包：

- set GOOS=linux #windows环境需要设置GOOS，才能build成linux环境的可运行二进制文件
- go build http_gin.go

![gin_compiler.png](/images/gin_compiler.png)

上传linux环境：

- 修改成可执行文件 chmod +x http_gin
- 运行 ./http_gin &

![gin_run.png](/images/gin_run.png)



### 3.2 springboot

关键代码：
```java
@RestController
public class DemoController {
    Result result = new Result("pong");

    @RequestMapping("/ping")
    public Result hello(){
        return result;
    }
}

class Result{
    String Message;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Result(String message) {
        Message = message;
    }
}
```

编译上传：
- maven编译 ：mvn install

运行：
- java -jar demo-0.0.1-SNAPSHOT.jar &

![springboot_run.png](/images/springboot_run.png)


## 4 benchmark

模拟20个用户，发出20万个请求

ab -c 20 -n 200000 http://172.16.60.211:8080/ping

### 4.1 gin benchmark

ab -c 20 -n 200000 http://172.16.60.211:8080/ping

benchmark结果:

```shell
Concurrency Level:      20
Time taken for tests:   13.423 seconds
Complete requests:      200000
Failed requests:        0
Write errors:           0
Total transferred:      28200000 bytes
HTML transferred:       3600000 bytes
Requests per second:    14900.02 [#/sec] (mean)
Time per request:       1.342 [ms] (mean)
Time per request:       0.067 [ms] (mean, across all concurrent requests)
Transfer rate:          2051.66 [Kbytes/sec] received
```

benchmark过程中，服务器CPU、内存状态：

![gin_cpu_mem.png](/images/gin_cpu_mem.png)

### 4.2 springboot benchmark
ab -c 10 -n 200000 http://172.16.60.211:8090/ping

```shell
Concurrency Level:      20
Time taken for tests:   17.336 seconds
Complete requests:      200000
Failed requests:        0
Write errors:           0
Total transferred:      24600000 bytes
HTML transferred:       3600000 bytes
Requests per second:    11536.65 [#/sec] (mean)
Time per request:       1.734 [ms] (mean)
Time per request:       0.087 [ms] (mean, across all concurrent requests)
Transfer rate:          1385.75 [Kbytes/sec] received
```

benchmark过程中，服务器CPU、内存状态：

![springboot_cpu_mem.png](/images/springboot_cpu_mem.png)

### 4.3 对比

|   | qps | CPU | 内存 | 包大小
|---|---|---|---|---
|gin| 14900 | 150% | 0.4% | 9M
|springboot|11536| 143% | 12% | 24M

结论：
- qps上，gin 比 springboot 高出1.3倍。别看只有1.3倍，如果公司现在有10000台服务器呢？
- CPU上，两者持平
- 内存上，gin比springboot 小30倍。这个差距是真有点大。
- 包大小上，gin比springboot 小2.6倍。别看磁盘只是小了2.6倍，流水线持续部署时，磁盘大小和每次传包的时间，也是相当可观的节省

从这些硬指标看，gin有具备比springboot更多的优势。但从社区看，springboot依然是个王者。springboot也做了webflow的支持，后续也可期待下这块的发展。

