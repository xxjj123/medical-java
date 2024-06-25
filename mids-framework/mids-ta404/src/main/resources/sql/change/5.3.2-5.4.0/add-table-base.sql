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
