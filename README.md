# minio_demo
***基于minio开源元对象存储使用spring boot制作的小demo，仅作为参考***

##接口
### 1.  `/view/up` 上传文件
* 请求参数
	* file 文件
	* bucket_path 要上传到具体的文件夹名称
*返回数据
	`{"code":200,"msg":"上传成功","url":""}`

### 2.`/view/list` 获取云对象文件夹列表
*返回数据
	`{"code":200,"msg":"success","data":[...]}`

###3. `/view/bucketlist` 获取指定文件夹中所有文件对象
*请求参数
	 *uri 文件夹名称（默认images）
*返回数据
	`{"code":200,"msg":"success","data":{{}}}`

###4. `/view/downloadfiles` 文件下载（GET）
*请求参数
	* downloadfiles 下载文件名

###5. `/view/getshareurl` 外链分享
***不传递任何参数默认分享有效期为1分钟***
*请求参数
	* filename 文件名
	* day 天(最大7天有效期)
	* hours 小时
	* minutes 分钟(默认一分钟)
*返回参数
	`{"code":200,"msg":"success","url":""}`

###6.`/view/fileinfo`文件信息
*请求参数
	* filename 文件名
*返回参数
	`{...}`文件详细信息
###7.`/view/delfiles` 删除文件(单个，批量)
*请求参数
	* data(list) 文件名数组
*返回参数
	`{"code":200,"msg":""}`

### 8.`/view/create_bucket`创建文件夹(存放文件对象的桶，不支持桶内再创建文件夹)
*请求参数
	* name 文件夹名称
*返回参数
	`{"code":200,"msg":""}`
