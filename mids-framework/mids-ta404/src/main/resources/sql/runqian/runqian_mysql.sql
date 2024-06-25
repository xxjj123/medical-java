DROP TABLE IF EXISTS TARUNQIANRESOURCE;
DROP TABLE IF EXISTS TARUNQIANPRINTSETUP;
DROP TABLE IF EXISTS TARUNQIANAD52REFERENCE;
create table TARUNQIANRESOURCE
(
    raqfilename       VARCHAR(200) not null primary key comment '文件名报表标识(raqfilename)',
    parentraqfilename VARCHAR(200) comment '父报表标识(ParentRaqfileName)',
    raqname           VARCHAR(200) comment '报表名称(RaqName)',
    parentraqname   VARCHAR(200) comment '父报表名称(ParentRaqName)',
    raqtype           VARCHAR(6) comment '报表类型(RaqType)',
    raqfile           longblob comment '资源文件(RaqFile)',
    uploador          VARCHAR(50) comment '上传人(Uploador)',
    uploadtime        TIMESTAMP comment '上传时间(UploadTime)',
    subrow            bigint comment '父报表位置行(SubRow)',
    subcell           bigint comment '父报表位置列(SubCell)',
    raqdatasource     VARCHAR(50) comment '数据源(RaqDataSource)',
    raqparam          VARCHAR(500) comment '报表参数JSON格式Str(RaqParam)',
    orgid             VARCHAR(40) comment '部门编号(OrgId)'
);
-- Add comments to the table
alter table TARUNQIANRESOURCE comment '润乾报表模板';


-- Create table
create table TARUNQIANPRINTSETUP
(
    setupid    VARCHAR(200) not null comment '打印设置编号(SetupId)',
    setupvalue VARCHAR(400) not null comment '打印设置信息(SetupValue)'
);
-- Add comments to the table
alter table TARUNQIANPRINTSETUP comment '打印设置信息表';

-- Create table
create table TARUNQIANAD52REFERENCE
(
    menuid              bigint comment '功能编号(Menuid)',
    raqfilename         VARCHAR(200) comment '文件名/报表标识(RaqfileName)',
    limited             bigint comment '每页显示数(Limited)',
    scaleexp            bigint comment 'JSP中缩放比率(ScaleExp)',
    isgroup             VARCHAR(6) comment '是否按行分页(IsGroup)',
    needsaveasexcel     VARCHAR(6) comment '是否保存为Excel(NeedSaveAsExcel)',
    needsaveasexcel2007 VARCHAR(6) comment '是否保存为Excel2007(NeedSaveAsExcel2007)',
    needsaveaspdf       VARCHAR(6) comment '是否保存为Pdf(NeedSaveAsPdf)',
    needsaveasword      VARCHAR(6) comment '是否保存为Word(NeedSaveAsWord)',
    needsaveastext      VARCHAR(6) comment '是否保存为Text(NeedSaveAsText)',
    needprint           VARCHAR(6) comment '是否保存为Print(NeedPrint)',
    id                  bigint not null comment '主键ID'
);
-- Add comments to the table
alter table  TARUNQIANAD52REFERENCE
    comment 'YHCIP_RUNQIAN_AD52_REFERENCE润乾报表菜单信息';

