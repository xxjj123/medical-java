
/* --------------- 创建表 --------------- */

CREATE TABLE TAUSERINFO(
                           `USERINFOID` VARCHAR(36) NOT NULL   COMMENT '用户编号' ,
                           `ORDERNO` DECIMAL(10)    COMMENT '排序号' ,
                           `NAME` VARCHAR(450)    COMMENT '姓名' ,
                           `SEX` VARCHAR(2)    COMMENT '性别' ,
                           `IDCARDTYPE` VARCHAR(2)    COMMENT '证件类型' ,
                           `IDCARDNO` VARCHAR(30)    COMMENT '证件号码' ,
                           `MOBILE` VARCHAR(20)    COMMENT '手机号码' ,
                           `CREATEUSER` VARCHAR(36)    COMMENT '创建人' ,
                           `CREATETIME` DATETIME    COMMENT '创建时间' ,
                           `MODIFYTIME` DATETIME    COMMENT '修改时间' ,
                           `ACCOUNTSOURCE` VARCHAR(2)    COMMENT '账户注册渠道' ,
                           `JOBNUMBER` VARCHAR(15)    COMMENT '工号' ,
                           `STATE` VARCHAR(10)    COMMENT '国家地区' ,
                           `BIRTHPLACE` VARCHAR(12)    COMMENT '户籍地（行政区划代码）' ,
                           `ADDRESS` VARCHAR(450)    COMMENT '联系地址' ,
                           `ZIPCODE` VARCHAR(10)    COMMENT '邮政编码' ,
                           `EMAIL` VARCHAR(100)    COMMENT '邮箱地址' ,
                           `PHONE` VARCHAR(20)    COMMENT '联系电话' ,
                           `EDUCATION` VARCHAR(30)    COMMENT '学历' ,
                           `GRADUATESCHOOL` VARCHAR(150)    COMMENT '毕业学校' ,
                           `WORKPLACE` VARCHAR(300)    COMMENT '工作单位' ,
                           `FIELD01` VARCHAR(1000)    COMMENT '扩展字段01' ,
                           `FIELD02` VARCHAR(1000)    COMMENT '扩展字段02' ,
                           `FIELD03` VARCHAR(1000)    COMMENT '扩展字段03' ,
                           `FIELD04` VARCHAR(1000)    COMMENT '扩展字段04' ,
                           `FIELD05` VARCHAR(1000)    COMMENT '扩展字段05' ,
                           `FIELD06` VARCHAR(1000)    COMMENT '扩展字段06' ,
                           `FIELD07` VARCHAR(1000)    COMMENT '扩展字段07' ,
                           `FIELD08` VARCHAR(1000)    COMMENT '扩展字段08' ,
                           `FIELD09` VARCHAR(1000)    COMMENT '扩展字段09' ,
                           `FIELD10` VARCHAR(1000)    COMMENT '扩展字段10' ,
                           `SPELL` VARCHAR(255)    COMMENT '拼音' ,
                           `DESTORY` VARCHAR(1)    COMMENT '销毁标识' ,
                           `EFFECTIVE` VARCHAR(1)    COMMENT '有效标识' ,
                           `EFFECTIVETIME` DATETIME    COMMENT '有效时间' ,
                           PRIMARY KEY (USERINFOID)
)  COMMENT = '人员表';

CREATE TABLE TAAPPLY(
                        `APPLYID` varchar(36)    COMMENT '申请编号' ,
                        `APPLYTYPE` VARCHAR(255)    COMMENT '申请类型;账号申请等' ,
                        `APPLYCONTENT` TEXT    COMMENT '申请内容;JSON' ,
                        `APPLYMESSAGE` VARCHAR(255)    COMMENT '申请说明' ,
                        `APPLYUSER` VARCHAR(255)    COMMENT '申请人信息;账号/身份证' ,
                        `APPLYTIME` DATETIME    COMMENT '申请时间' ,
                        `STATUS` VARCHAR(1)    COMMENT '审核状态;待审核、审核通过、审核不通过' ,
                        `MESSAGE` VARCHAR(255)    COMMENT '审核说明;审核应是允许扩展的，如可引入流程引擎实现多级审核' ,
                        `AUDITUSER` VARCHAR(255)    COMMENT '审核人信息' ,
                        `AUDITTIME` DATETIME    COMMENT '审核时间' ,
                        `ASSOCIATEDENTITYID` VARCHAR(255)    COMMENT '关联实体ID;账号ID等' ,
                        `APPLYAUTHKEY` VARCHAR(255)    COMMENT '申请权限标识;比如：组织ID，多个逗号隔开' ,
                        `APPLYAUTHTYPE` VARCHAR(255)    COMMENT '申请权限类型;比如：组织' ,
                        PRIMARY KEY (APPLYID)
)  COMMENT = '申请审核表';

CREATE TABLE TAUSERFAMILY(
                             `USERID` VARCHAR(36)    COMMENT '账号ID' ,
                             `FAMILYUSERID` VARCHAR(36)    COMMENT '关联账号ID' ,
                             `STATUS` VARCHAR(1)    COMMENT '状态;代对方确认、已绑定、已拒绝'
)  COMMENT = '自然人亲情账号关系表';

CREATE TABLE TAORGVIEW(
                          `ROLEID` varchar(36)    COMMENT '角色ID' ,
                          `ORGID` varchar(36)    COMMENT '组织ID' ,
                          PRIMARY KEY (ROLEID,ORGID)
)  COMMENT = '角色组织可见范围表';

CREATE TABLE TAPARAM(
                        `PARAMID` varchar(36)    COMMENT '参数ID' ,
                        `PARAMNAME` VARCHAR(255)    COMMENT '参数名称' ,
                        `VALUESCOPE` VARCHAR(255)    COMMENT '值范围' ,
                        `CODE` VARCHAR(255)    COMMENT '编码' ,
                        `PARAMDESC` VARCHAR(255)    COMMENT '参数说明' ,
                        `VALUE` VARCHAR(255)    COMMENT '值' ,
                        `RESOURCEID` VARCHAR(36) NOT NULL   COMMENT '资源ID' ,
                        PRIMARY KEY (PARAMID)
)  COMMENT = '资源参数表';

CREATE TABLE TASYSBEHAVIORLOG(
                                 `BEHAVIORLOGID` VARCHAR(36) NOT NULL   COMMENT '主键' ,
                                 `SERVICENAME` VARCHAR(36)    COMMENT '服务名' ,
                                 `SERVICEPATH` VARCHAR(120)    COMMENT '服务路径' ,
                                 `SERVICEPARAMCONTENT` TEXT    COMMENT '服务请求参数内容' ,
                                 `RESOURCEID` VARCHAR(36)    COMMENT '菜单ID' ,
                                 `BUSINESSTYPE` VARCHAR(256)    COMMENT '业务类型' ,
                                 `BEGINTIME` VARCHAR(36)    COMMENT '开始时间' ,
                                 `ENDTIME` VARCHAR(36)    COMMENT '结束时间' ,
                                 `TIMEUSED` VARCHAR(36)    COMMENT '执行时间' ,
                                 `SUCCESSFLAG` VARCHAR(36)    COMMENT '是否异常标志' ,
                                 `TRACEID` VARCHAR(36)    COMMENT '链路ID' ,
                                 `LOGINWAY` VARCHAR(36)    COMMENT '登录方式' ,
                                 `CLIENTIP` VARCHAR(36)    COMMENT '客户端IP' ,
                                 `USERID` VARCHAR(36)    COMMENT '经办人ID'
)  COMMENT = '行为日志表';

CREATE TABLE TASYSBEHAVIORLOGDETAIL(
                                       `BEHAVIORLOGDETAILID` VARCHAR(36) NOT NULL   COMMENT '主键' ,
                                       `BEHAVIORLOGID` VARCHAR(36)    COMMENT '行为日志ID' ,
                                       `OPERATETYPE` VARCHAR(36)    COMMENT '操作对象类型' ,
                                       `OPERATEOBJECTID` VARCHAR(36)    COMMENT '操作对象ID' ,
                                       `OPERATCONTET` VARCHAR(256)    COMMENT '操作行为描述' ,
                                       `BUSINESSTYPE` VARCHAR(256)    COMMENT '业务类型' ,
                                       `BUSINESSID` VARCHAR(36)    COMMENT '业务编号' ,
                                       `BEGINTIME` VARCHAR(36)    COMMENT '开始时间' ,
                                       `ENDTIME` VARCHAR(36)    COMMENT '结束时间' ,
                                       `TIMEUSED` VARCHAR(36)    COMMENT '执行时间' ,
                                       `SUCCESSFLAG` VARCHAR(36)    COMMENT '成功标志'
)  COMMENT = '行为日志细节表';

-- 访问系统权限控制表
CREATE TABLE `taaccessauthority` (
                                     `accessid` varchar(100) NOT NULL,
                                     `urlid` varchar(100) NOT NULL,
                                     PRIMARY KEY (`accessid`,`urlid`),
                                     KEY `taaccessauthority_accessid_IDX` (`accessid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='访问系统权限控制表';
-- 第三方系统访问权限控制功能添加
CREATE TABLE `taaccessconfig` (
                                  `accessid` varchar(36) NOT NULL COMMENT '访问标识',
                                  `pubkey` varchar(512) NOT NULL COMMENT '公钥',
                                  `accessName` varchar(100) NOT NULL COMMENT '访问系统描述',
                                  `crypto` varchar(100) NOT NULL COMMENT '加密算法  sm2/rsa/ecdsa等',
                                  PRIMARY KEY (`accessid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='访问系统公钥配置表';

-- 代理授权添加
CREATE TABLE `taagent` (
                           `userid` varchar(100) NOT NULL,
                           `agentid` varchar(100) NOT NULL,
                           `roleid` varchar(100) NOT NULL,
                           `effectivetime` datetime DEFAULT NULL,
                           PRIMARY KEY (`userid`,`agentid`,`roleid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT = '个人代理表';

-- 访问限流功能添加
CREATE TABLE `taratelimit` (
                               `urlid` varchar(100) NOT NULL,
                               `rate` decimal(10,0) DEFAULT NULL,
                               `maxcount` bigint(20) DEFAULT NULL,
                               `timeout` bigint(20) DEFAULT NULL,
                               `enable` varchar(100) DEFAULT NULL,
                               PRIMARY KEY (`urlid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='url访问限流配置表';

CREATE TABLE TAREGISTERCHANNEL(
                                  `REGISTERCHANNELID` VARCHAR(64) NOT NULL   COMMENT '' ,
                                  `REGISTERCHANNELNAME` VARCHAR(100) NOT NULL   COMMENT '' ,
                                  `REGISTERCHANNELURL` VARCHAR(100)    COMMENT '' ,
                                  `CREATEUSER` VARCHAR(64)    COMMENT '' ,
                                  `CREATETIME` TIMESTAMP   NULL  COMMENT '' ,
                                  `EFFECTIVETIME` TIMESTAMP  NULL  COMMENT '' ,
                                  `EFFECTIVE` VARCHAR(1)    COMMENT '' ,
                                  `ORGID` VARCHAR(64)    COMMENT '' ,
                                  `ROLEID` VARCHAR(64)    COMMENT '' ,
                                  PRIMARY KEY (REGISTERCHANNELID)
)  COMMENT = '注册通道表';

create table TAREDISSEQUENCE
(
    BIZ_TAG              varchar(36) not null comment '序列名称',
    START_INDEX          numeric(20,0) not null comment '起始值',
    MAX_ID               numeric(20,0) not null comment '最新值',
    STEP                 numeric(10,0) not null comment '步长',
    primary key (BIZ_TAG)
) COMMENT = 'REDIS 序列信息表';


create table TAWORKERNODE
(
    ID              numeric(10,0) not null comment '工作节点ID',
    HOST_NAME       varchar(36) not null comment 'host',
    PORT            varchar(36) not null comment 'port',
    primary key (ID)
) COMMENT = 'SNOWFLOW 序列工作节点信息表';
