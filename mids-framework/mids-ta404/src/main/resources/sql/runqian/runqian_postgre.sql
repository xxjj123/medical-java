create table TARUNQIANRESOURCE
(
  raqfilename       VARCHAR(200) not null primary key,
  parentraqfilename VARCHAR(200),
  raqname           VARCHAR(200),
  parentraqname   VARCHAR(200),
  raqtype           VARCHAR(6),
  raqfile           bytea ,
  uploador          VARCHAR(50),
  uploadtime        TIMESTAMP,
  subrow            bigint,
  subcell           bigint,
  raqdatasource     VARCHAR(50),
  raqparam          VARCHAR(500),
  orgid             VARCHAR(40)
);
-- Add comments to the table
comment on table TARUNQIANRESOURCE
  is '润乾报表模板';
-- Add comments to the columns
comment on column TARUNQIANRESOURCE.raqfilename
  is '文件名/报表标识（RaqfileName）';
comment on column TARUNQIANRESOURCE.parentraqfilename
  is '父报表标识（ParentRaqfileName）';
comment on column TARUNQIANRESOURCE.raqname
  is '报表名称（RaqName）';
  comment on column TARUNQIANRESOURCE.parentraqname
  is '父报表名称（ParentRaqName）';
comment on column TARUNQIANRESOURCE.raqtype
  is '报表类型（RaqType）';
comment on column TARUNQIANRESOURCE.raqfile
  is '资源文件（RaqFile）';
comment on column TARUNQIANRESOURCE.uploador
  is '上传人（Uploador）';
comment on column TARUNQIANRESOURCE.uploadtime
  is '上传时间（UploadTime）';
comment on column TARUNQIANRESOURCE.subrow
  is '父报表位置行（SubRow）';
comment on column TARUNQIANRESOURCE.subcell
  is '父报表位置列（SubCell）';
comment on column TARUNQIANRESOURCE.raqdatasource
  is '数据源（RaqDataSource）';
comment on column TARUNQIANRESOURCE.raqparam
  is '报表参数JSON格式Str（RaqParam）';
comment on column TARUNQIANRESOURCE.orgid
  is '部门编号(OrgId)';


