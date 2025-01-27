SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for `t_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '用户ID(自增长)',
    `user_name`   varchar(64) NOT NULL COMMENT '用户名',
    `address`     varchar(64) NOT NULL COMMENT '用户钱包地址',
    `status`      tinyint(4)  NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `create_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_ADDRESS` (`address`) USING BTREE COMMENT '用户地址唯一',
    UNIQUE KEY `UK_USERNAME` (`user_name`) USING BTREE COMMENT '用户名称唯一'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';


-- ----------------------------
-- Table structure for `t_organization`
-- ----------------------------
DROP TABLE IF EXISTS `t_organization`;
CREATE TABLE `t_organization`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组织表ID(自增长)',
    `node_name`   varchar(100)        DEFAULT NULL COMMENT '组织的身份名称',
    `node_id`     varchar(256)        DEFAULT NULL COMMENT '组织中调度服务的 nodeId',
    `identity_id` varchar(128)        DEFAULT NULL COMMENT '组织的身份标识Id',
    `identity_ip` varchar(20)        DEFAULT NULL COMMENT '组织的ip',
    `identity_port` int(11)        DEFAULT NULL COMMENT '组织的端口',
    `public_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否公有可查节点: 0-否，1- 是',
    `status`      tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-未知，1- 正常， 2- 异常',
    `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_ORG_IDENTITY_ID` (`identity_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='组织表';


-- ----------------------------
-- Table structure for `t_user_org_maintain`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_org_maintain`;
CREATE TABLE `t_user_org_maintain` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户组织连接绑定关系表ID(自增长)',
    `address` varchar(64) NOT NULL COMMENT '用户钱包地址',
    `identity_id` varchar(128) DEFAULT NULL COMMENT '组织的身份标识Id',
    `identity_ip` varchar(20) DEFAULT NULL COMMENT '组织的ip',
    `identity_port` int(11) DEFAULT NULL COMMENT '组织的端口',
    `valid_flag` tinyint(4) NOT NULL DEFAULT '1' COMMENT '连接有效状态: 0-无效，1- 有效',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: 0-未知，1- 正常， 2- 异常',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_USER_IDENTITY_ID` (`address`,`identity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户组织连接绑定关系表';

-- ----------------------------
-- Table structure for `t_user_org`
-- 是否需要同步机构待底层联调结果调整
-- ----------------------------
DROP TABLE IF EXISTS `t_user_org`;
CREATE TABLE `t_user_org`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '机构绑定表ID(自增长)',
    `user_id`       bigint(20) NOT NULL COMMENT '用户ID',
    `identity_id`   varchar(128)        DEFAULT NULL COMMENT '组织的身份标识Id',
    `identity_name` varchar(100)        DEFAULT NULL COMMENT '组织的身份名称',
    `bind_status`   tinyint(4) NOT NULL DEFAULT 0 COMMENT '绑定状态: 0-未绑定, 1-已绑定, 2-已拒绝',
    `create_time`   timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_USER_IDENTITY_ID` (`user_id`, `identity_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户绑定机构表';

-- ----------------------------
-- Table structure for `t_login_log`
-- ----------------------------
DROP TABLE IF EXISTS `t_login_log`;
CREATE TABLE `t_login_log`
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '日志表id(自增长)',
    `user_id`       bigint(20)  NOT NULL COMMENT '用户id',
    `login_status`  tinyint(4)  NOT NULL DEFAULT 1 COMMENT '登录状态: 0-登录失败, 1-登录成功',
    `login_ip`      varchar(32) NOT NULL COMMENT '登录ip',
    `login_browser` varchar(16) NOT NULL COMMENT '浏览器',
    `create_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户登录日志表';

-- ----------------------------
-- Table structure for `t_meta_data`
-- ----------------------------
DROP TABLE IF EXISTS `t_meta_data`;
CREATE TABLE `t_meta_data`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '元数据表ID(自增长)',
    `identity_id`   varchar(128)          DEFAULT NULL COMMENT '资源所属组织的身份标识Id',
    `identity_name` varchar(64)           DEFAULT NULL COMMENT '资源所属组织名称',
    `node_id`       varchar(256)          DEFAULT NULL COMMENT '资源所属组织中调度服务的 nodeId',
    `meta_data_id`  varchar(128)          DEFAULT NULL COMMENT '元数据id',
    `file_id`       varchar(256)          DEFAULT NULL COMMENT '源文件ID',
    `data_name`     varchar(128) NOT NULL COMMENT '元数据名称|数据名称 (表名)',
    `data_desc`     varchar(128) NOT NULL COMMENT '元数据的描述 (摘要)',
    `file_path`     varchar(128) NOT NULL COMMENT '源文件存放路径',
    `rows`          int(11)      NOT NULL DEFAULT '0' COMMENT '源文件的行数',
    `columns`       int(11)      NOT NULL DEFAULT '0' COMMENT '源文件的列数',
    `size`          bigint(20)   NOT NULL DEFAULT '0' COMMENT '源文件的大小 (单位: byte)',
    `file_type`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '源文件类型: 0-未知，1- CSV类型',
    `has_title`     tinyint(4)   NOT NULL DEFAULT '0' COMMENT '是否带标题,0表示不带，1表示带标题',
    `industry`      varchar(20)           DEFAULT NULL COMMENT '元数据所属行业  1：金融业（银行）、2：金融业（保险）、3：金融业（证券）、4：金融业（其他）、5：ICT、 6：制造业、 7：能源业、 8：交通运输业、 9 ：医疗健康业、 10 ：公共服务业、 11：传媒广告业、 12 ：其他行业',
    `data_status`   tinyint(4)   NOT NULL DEFAULT '1' COMMENT '元数据的状态 (0-未知; 1- 还未发布的新表; 2- 已发布的表; 3- 已撤销的表)',
    `status`        tinyint(4)   NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `publish_at`    datetime     DEFAULT NULL         COMMENT '元数据发布时间',
    `update_at`     datetime     DEFAULT NULL         COMMENT '元数据更新时间',
    `create_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_META_DATA_ID` (`meta_data_id`, `data_status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='元数据表';

-- ----------------------------
-- Table structure for `t_meta_data_details`
-- ----------------------------
DROP TABLE IF EXISTS `t_meta_data_details`;
CREATE TABLE `t_meta_data_details`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据详情表ID(自增长)',
    `meta_data_id` varchar(128)        DEFAULT NULL COMMENT '元数据id',
    `column_index` int(11)             DEFAULT NULL COMMENT '列索引',
    `column_name`  varchar(256)         DEFAULT NULL COMMENT '列名',
    `column_type`  varchar(32)         DEFAULT NULL COMMENT '列类型',
    `column_size`  bigint(20)          DEFAULT '0' COMMENT '列大小（byte）',
    `column_desc`  varchar(256)         DEFAULT NULL COMMENT '列描述',
    `status`       tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_META_DATA_COLUMN_ID` (`meta_data_id`, `column_index`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='元数据列详情表';

-- ----------------------------
-- Table structure for `t_user_meta_data`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_meta_data`;
CREATE TABLE `t_user_meta_data`
(
    `id`                  bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '数据详情表ID(自增长)',
    `meta_data_id`        varchar(128)         DEFAULT NULL COMMENT '元数据id',
    `identity_id`         varchar(128)         DEFAULT NULL COMMENT '资源所属组织的身份标识Id',
    `identity_name`       varchar(64)          DEFAULT NULL COMMENT '资源所属组织名称',
    `node_id`             varchar(256)         DEFAULT NULL COMMENT '资源所属组织中调度服务的 nodeId',
    `address`             varchar(64) NOT NULL COMMENT '用户钱包地址',
    `auth_type`           tinyint(4)           DEFAULT NULL COMMENT '授权方式: 1-按时间, 2-按次数, 3-永久',
    `auth_value`          bigint(20)           DEFAULT NULL COMMENT '授权值:按次数单位为（次）',
    `auth_begin_time`     datetime             DEFAULT NULL COMMENT '授权开始时间',
    `auth_end_time`       datetime             DEFAULT NULL COMMENT '授权结束时间',
    `auth_status`         tinyint(4)           DEFAULT NULL COMMENT '授权状态: 0-等待审核中, 1-审核通过, 2-审核拒绝',
    `audit_suggestion`    varchar(256)         DEFAULT NULL COMMENT '审核意见 ',
    `apply_time`          datetime             DEFAULT NULL COMMENT '发起授权申请的时间',
    `audit_time`          datetime             DEFAULT NULL COMMENT '审核授权申请的时间',
    `auth_metadata_state` tinyint(4)           DEFAULT '0' COMMENT '数据授权信息的状态 (0: 未知; 1: 还未发布的数据授权; 2: 已发布的数据授权; 3: 已撤销的数据授权 <失效前主动撤回的>; 4: 已经失效的数据授权 <过期or达到使用上限的or被拒绝的>;)',
    `expire`              tinyint(4)           DEFAULT NULL COMMENT '是否已过期（按时间时需要）: 0-未过期, 1-已过期',
    `used_times`          bigint(20)           DEFAULT '0' COMMENT '已经使用的次数(按次数时有效)',
    `metadata_auth_id`        varchar(256)         DEFAULT NULL COMMENT '元数据授权申请返回唯一id',
    `status`              tinyint(4)  NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `create_time`         timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_META_AUTH_ID` (`metadata_auth_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户元数据授权表';

-- ----------------------------
-- Table structure for `t_project`
-- ----------------------------
DROP TABLE IF EXISTS `t_project`;
CREATE TABLE `t_project`
(
    `id`           bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '项目ID(自增长)',
    `user_id`      bigint(20)           DEFAULT NULL COMMENT '用户id(创建者id)',
    `project_name` varchar(30) NOT NULL COMMENT '项目名称',
    `project_desc` varchar(200)         DEFAULT NULL COMMENT '项目描述',
    `del_version`  bigint(11)           DEFAULT 0 COMMENT '版本标识，用于逻辑删除',
    `status`       tinyint(4)  NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_NAME` (`project_name`, `status`, `del_version`),
    KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目表';

-- ----------------------------
-- Table structure for `t_project_temp`
-- ----------------------------
DROP TABLE IF EXISTS `t_project_temp`;
CREATE TABLE `t_project_temp`
(
    `id`           bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '项目模板表ID(自增长)',
    `project_name` varchar(30) NOT NULL COMMENT '中文项目名称',
    `project_name_en` varchar(30) NOT NULL COMMENT '英文项目名称',
    `project_desc` varchar(200)         DEFAULT NULL COMMENT '中文项目描述',
    `project_desc_en` varchar(200)         DEFAULT NULL COMMENT '英文项目描述',
    `status`       tinyint(4)  NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_NAME` (`project_name`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目模板表';


-- ----------------------------
-- Table structure for `t_project_member`
-- ----------------------------
DROP TABLE IF EXISTS `t_project_member`;
CREATE TABLE `t_project_member`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目成员ID(自增长)',
    `project_id`  bigint(20) NOT NULL COMMENT '项目id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户id',
    `role`        tinyint(4)          DEFAULT NULL COMMENT '角色：1-管理员，2-编辑着, 3-查看着',
    `status`      tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_PROJ_USER_ID` (`project_id`, `user_id`),
    KEY (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目成员管理表';

-- ----------------------------
-- Table structure for `t_algorithm_type`
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_type`;
CREATE TABLE `t_algorithm_type`
(
    `id`                  bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法表ID(自增长)',
    `algorithm_type_name` varchar(30)         DEFAULT NULL COMMENT '中文算法名称',
    `algorithm_type_name_en` varchar(60)         DEFAULT NULL COMMENT '英文算法名称',
    `algorithm_type_desc` varchar(200)        DEFAULT NULL COMMENT '中文算法描述',
    `algorithm_type_desc_en` varchar(200)        DEFAULT NULL COMMENT '英文算法描述',
    `algorithm_type`      tinyint(4)          DEFAULT NULL COMMENT '算法所属大类:1-统计分析,2-特征工程,3-机器学习',
    `status`              tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1-有效',
    `create_time`         timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='算法大类表';

-- ----------------------------
-- Table structure for `t_algorithm`
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm`;
CREATE TABLE `t_algorithm`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法表ID(自增长)',
    `algorithm_name`    varchar(60)         DEFAULT NULL COMMENT '中文算法名称',
    `algorithm_name_en`    varchar(60)         DEFAULT NULL COMMENT '英文算法名称',
    `algorithm_desc`    varchar(200)        DEFAULT NULL COMMENT '中文算法描述',
    `algorithm_desc_en`    varchar(200)        DEFAULT NULL COMMENT '英文算法描述',
    `author`            varchar(30)         DEFAULT NULL COMMENT '算法作者',
    `max_numbers`       bigint(20)          DEFAULT NULL COMMENT '支持协同方最大数量',
    `min_numbers`       bigint(20)          DEFAULT NULL COMMENT '支持协同方最小数量',
    `support_language`  varchar(64)         DEFAULT NULL COMMENT '支持语言,多个以","进行分隔',
    `support_os_system` varchar(64)         DEFAULT NULL COMMENT '支持操作系统,多个以","进行分隔',
    `algorithm_type`    tinyint(4)          DEFAULT NULL COMMENT '算法所属大类:1-统计分析,2-特征工程,3-机器学习',
    `cost_mem`          bigint(20)          DEFAULT NULL COMMENT '所需的内存 (单位: byte)',
    `cost_cpu`          int(11)             DEFAULT NULL COMMENT '所需的核数 (单位: 个)',
    `cost_gpu`          int(11)             DEFAULT NULL COMMENT 'GPU核数(单位：核)',
    `cost_bandwidth`    bigint(20)          DEFAULT '0' COMMENT '所需的带宽 (单位: bps)',
    `run_time`          bigint(20) NOT NULL DEFAULT '3600000' COMMENT '所需的运行时长,默认1小时 (单位: ms)',
    `input_model`       tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否需要输入模型: 0-否，1:是',
    `store_pattern`     tinyint(4) NOT NULL DEFAULT '1' COMMENT '输出存储形式: 1-明文，2:密文',
    `data_rows_flag`         tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否判断数据行数: 0-否，1-是',
    `data_columns_flag`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否判断数据列数: 0-否，1-是',
    `public_Flag`       tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否是公有算法: 0-否，1-是',
    `status`            tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1-有效',
    `create_time`       timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_ALG_NAME` (`algorithm_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='算法表';

-- ----------------------------
-- Table structure for `t_algorithm_auth`
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_auth`;
CREATE TABLE `t_algorithm_auth`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法授权表ID(自增长)',
    `user_id`         bigint(20) NOT NULL COMMENT '用户id',
    `algorithm_id`    bigint(20) NOT NULL COMMENT '算法表id',
    `auth_type`       tinyint(4)          DEFAULT NULL COMMENT '授权方式: 1-按时间, 2-按次数, 3-永久',
    `auth_value`      bigint(20)          DEFAULT NULL COMMENT '授权值: 按次数单位为（次）',
    `auth_begin_time` datetime            DEFAULT NULL COMMENT '授权开始时间',
    `auth_end_time`   datetime            DEFAULT NULL COMMENT '授权结束时间',
    `auth_status`     tinyint(4) NOT NULL DEFAULT 0 COMMENT '授权状态: 0-待申请,1-申请中, 2-已授权,3-已拒绝',
    `status`          tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`     timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `user_algorithm_id` (`user_id`, `algorithm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='算法授权表';

-- ----------------------------
-- Table structure for `t_algorithm_code`
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_code`;
CREATE TABLE `t_algorithm_code`
(
    `id`                       bigint(20) NOT NULL AUTO_INCREMENT COMMENT '算法代码表ID(自增长)',
    `algorithm_id`             bigint(20)          DEFAULT NULL COMMENT '算法id',
    `edit_type`                tinyint(4)          DEFAULT NULL COMMENT '编辑类型:1-sql,2-noteBook',
    `calculate_contract_code`  TEXT                DEFAULT NULL COMMENT '计算合约',
    `data_split_contract_code` TEXT                DEFAULT NULL COMMENT '数据分片合约',
    `status`                   tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`              timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`algorithm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='算法代码表';

-- ----------------------------
-- Table structure for `t_algorithm_variable`
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_variable`;
CREATE TABLE `t_algorithm_variable`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '算法变量表ID(自增长)',
    `algorithm_id` bigint(20)            DEFAULT NULL COMMENT '算法表id',
    `var_type`     tinyint(4)   NOT NULL DEFAULT 1 COMMENT '变量类型: 1-自变量, 2-因变量',
    `var_key`      varchar(128) NOT NULL COMMENT '变量key',
    `var_value`    varchar(128) NOT NULL COMMENT '变量值',
    `var_desc`     varchar(512)          DEFAULT NULL COMMENT '变量描述',
    `status`       tinyint(4)   NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `create_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`algorithm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='算法变量表';


-- ----------------------------
-- Table structure for `t_algorithm_variable_struct`
-- ----------------------------
DROP TABLE IF EXISTS `t_algorithm_variable_struct`;
CREATE TABLE `t_algorithm_variable_struct`
(
    `id`           bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '算法变量模板结构表ID(自增长)',
    `algorithm_id` bigint(20)             DEFAULT NULL COMMENT '算法表id',
    `struct`       varchar(1024) NOT NULL COMMENT '模板json格式结构',
    `desc`         varchar(128)           DEFAULT NULL COMMENT '模板描述',
    `status`       tinyint(4)    NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `create_time`  timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`algorithm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='算法变量模板结构表';

-- ----------------------------
-- Table structure for `t_workflow`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow`;
CREATE TABLE `t_workflow`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流ID(自增长)',
    `project_id`    bigint(20)          DEFAULT NULL COMMENT '项目id',
    `user_id`       bigint(20)          DEFAULT NULL COMMENT '用户id(创建方id)',
    `workflow_name` varchar(60)         DEFAULT NULL COMMENT '工作流名称',
    `workflow_desc` varchar(200)        DEFAULT NULL COMMENT '工作流描述',
    `node_number`   int(11)             DEFAULT NULL COMMENT '节点数',
    `address`       varchar(64)         DEFAULT NULL COMMENT '发起任务的账户',
    `sign`          varchar(512)        DEFAULT NULL COMMENT '发起任务的账户的签名',
    `run_status`    tinyint(4) NOT NULL DEFAULT 0 COMMENT '运行状态:0-未运行,1-运行中,2-运行成功，3-运行失败',
    `del_version`   bigint(11)          DEFAULT 0 COMMENT '版本标识，用于逻辑删除',
    `status`        tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`   timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY UK_FLOW_NAME (`project_id`, `workflow_name`, `status`, `del_version`),
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流表';

-- ----------------------------
-- Table structure for `t_workflow_temp`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_temp`;
CREATE TABLE `t_workflow_temp`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流模板ID(自增长)',
    `project_temp_id` bigint(20)          DEFAULT NULL COMMENT '项目模板表id',
    `workflow_name`   varchar(64)         DEFAULT NULL COMMENT '中文工作流名称',
    `workflow_name_en`   varchar(128)         DEFAULT NULL COMMENT '英文工作流名称',
    `workflow_desc`   varchar(128)        DEFAULT NULL COMMENT '中文工作流描述',
    `workflow_desc_en`   varchar(128)        DEFAULT NULL COMMENT '英文工作流描述',
    `node_number`     int(11)             DEFAULT NULL COMMENT '节点数',
    `run_status`      tinyint(4) NOT NULL DEFAULT 0 COMMENT '运行状态:0-未完成,1-已完成',
    `status`          tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`     timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流模板表';

-- ----------------------------
-- Table structure for `t_workflow_node`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node`;
CREATE TABLE `t_workflow_node`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流节点表ID(自增长)',
    `workflow_id`    bigint(20)          DEFAULT NULL COMMENT '工作流id',
    `algorithm_id`   bigint(20)          DEFAULT NULL COMMENT '算法id',
    `node_name`      varchar(30)         DEFAULT NULL COMMENT '节点名称',
    `node_step`      int(11)             DEFAULT NULL COMMENT '节点在工作流中序号,从1开始',
    `next_node_step` int(11)             DEFAULT NULL COMMENT '下一个节点,如果为空则无下个节点',
    `model_id`       bigint(20)          DEFAULT NULL COMMENT '工作流节点需要的模型id,对应t_task_result表id',
    `run_status`     tinyint(4)          DEFAULT '0' COMMENT '运行状态:0-未开始,1-运行中,2-运行成功,3-运行失败',
    `task_id`        varchar(256)        DEFAULT NULL COMMENT '任务ID,底层处理完成后返回',
    `run_msg`        varchar(256)        DEFAULT NULL COMMENT '任务处理结果描述',
    `status`         tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `create_time`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_NODE_STEP` (`workflow_id`, `node_step`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流节点表';

-- ----------------------------
-- Table structure for `t_workflow_node_temp`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node_temp`;
CREATE TABLE `t_workflow_node_temp`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流节点模板表ID(自增长)',
    `workflow_temp_id` bigint(20)          DEFAULT NULL COMMENT '工作流模板表id',
    `algorithm_id`     bigint(20)          DEFAULT NULL COMMENT '算法id',
    `node_name`        varchar(30)         DEFAULT NULL COMMENT '中文节点名称',
    `node_name_en`        varchar(60)         DEFAULT NULL COMMENT '英文节点名称',
    `node_step`        int(11)             DEFAULT NULL COMMENT '节点在工作流中序号,从1开始',
    `next_node_step`   int(11)             DEFAULT NULL COMMENT '下一个节点,如果为空则无下个节点',
    `run_status`       tinyint(4)          DEFAULT NULL COMMENT '运行状态:0-未开始,1-运行中,2-运行成功,3-运行失败',
    `status`           tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`      timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流节点模板表';

-- ----------------------------
-- Table structure for `t_workflow_node_input`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node_input`;
CREATE TABLE `t_workflow_node_input`
(
    `id`                 bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流节点输入表ID(自增长)',
    `workflow_node_id`   bigint(20)          DEFAULT NULL COMMENT '工作流节点id',
    `identity_id`        varchar(128)        DEFAULT NULL COMMENT '组织的身份标识Id',
    `data_table_id`      varchar(128)        DEFAULT NULL COMMENT '数据表ID',
    `key_column`         bigint(20)          DEFAULT NULL COMMENT 'ID列(列索引)(存id值)',
    `dependent_variable` bigint(20)          DEFAULT NULL COMMENT '因变量(标签)(存id值)',
    `data_column_ids`    varchar(1024)       DEFAULT NULL COMMENT '数据字段ID索引(存id值)',
    `data_file_id`       varchar(128)        DEFAULT NULL COMMENT '数据文件id',
    `sender_flag`        tinyint(4)          DEFAULT NULL COMMENT '是否发起方: 0-否,1-是',
    `party_id`           varchar(64)         DEFAULT NULL COMMENT '任务里面定义的 (p0 -> pN 方 ...)',
    `status`             tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`        timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`workflow_node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流节点输入表';

-- ----------------------------
-- Table structure for `t_workflow_node_variable`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node_variable`;
CREATE TABLE `t_workflow_node_variable`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '工作流节点变量表ID(自增长)',
    `workflow_node_id` bigint(20)            DEFAULT NULL COMMENT '工作流节点id',
    `var_node_type`    tinyint(4)   NOT NULL DEFAULT 1 COMMENT '变量类型: 1-自变量, 2-因变量',
    `var_node_key`     varchar(128) NOT NULL COMMENT '变量key',
    `var_node_value`   varchar(128) NOT NULL COMMENT '变量值',
    `var_node_desc`    varchar(200)          DEFAULT NULL COMMENT '变量描述',
    `status`           tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流节点变量表';

-- ----------------------------
-- Table structure for `t_workflow_node_output`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node_output`;
CREATE TABLE `t_workflow_node_output`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流节点输出表ID(自增长)',
    `workflow_node_id` bigint(20)          DEFAULT NULL COMMENT '工作流节点id',
    `identity_id`      varchar(128)        DEFAULT NULL COMMENT '协同方组织的身份标识Id',
    `party_id`         varchar(64)         DEFAULT NULL COMMENT '任务里面定义的 (p0 -> pN 方 ...)',
    `sender_flag`      tinyint(4)          DEFAULT NULL COMMENT '是否发起方: 0-否,1-是',
    `store_pattern`    tinyint(4)          DEFAULT 1 COMMENT '存储形式: 1-明文，2:密文',
    `store_path`       varchar(200)        DEFAULT NULL COMMENT '存储路径',
    `output_content`   TEXT                DEFAULT NULL COMMENT '输出内容',
    `status`           tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`      timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`workflow_node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目工作流节点输出表';

-- ----------------------------
-- Table structure for `t_workflow_node_code`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node_code`;
CREATE TABLE `t_workflow_node_code`
(
    `id`                       bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流节点算法代码表ID(自增长)',
    `workflow_node_id`         bigint(20)          DEFAULT NULL COMMENT '工作流节点id',
    `edit_type`                tinyint(4)          DEFAULT NULL COMMENT '编辑类型:1-sql, 2-noteBook',
    `calculate_contract_code`  TEXT                DEFAULT NULL COMMENT '计算合约',
    `data_split_contract_code` TEXT                DEFAULT NULL COMMENT '数据分片合约',
    `status`                   tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`              timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`workflow_node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流节点算法代码表';

-- ----------------------------
-- Table structure for `t_workflow_node_resource`
-- ----------------------------
DROP TABLE IF EXISTS `t_workflow_node_resource`;
CREATE TABLE `t_workflow_node_resource`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工作流节点资源表ID(自增长)',
    `workflow_node_id` bigint(20)          DEFAULT NULL COMMENT '工作流节点id',
    `cost_mem`         bigint(20)          DEFAULT NULL COMMENT '所需的内存 (单位: byte)',
    `cost_cpu`         int(20)             DEFAULT NULL COMMENT '所需的核数 (单位: 个)',
    `cost_gpu`         int(11)             DEFAULT NULL COMMENT 'GPU核数(单位：核)',
    `cost_bandwidth`   bigint(20)          DEFAULT 0 COMMENT '所需的带宽 (单位: bps)',
    `run_time`         bigint(20)          DEFAULT NULL COMMENT '所需的运行时长 (单位: ms)',
    `status`           tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`      timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`workflow_node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='工作流节点资源表';

-- ----------------------------
-- Table structure for `t_job`
-- ----------------------------
DROP TABLE IF EXISTS `t_job`;
CREATE TABLE `t_job`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务计划表ID(自增长)',
    `workflow_id`     bigint(20)          DEFAULT NULL COMMENT '工作流id',
    `name`            varchar(30)         DEFAULT NULL COMMENT '作业名称',
    `desc`            varchar(200)         DEFAULT NULL COMMENT '作业描述',
    `repeat_flag`     tinyint(4)          DEFAULT '1' COMMENT '是否重复：0-否,1-是',
    `repeat_interval` int(11)             DEFAULT NULL COMMENT '重复间隔，单位分钟',
    `begin_time`      datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `end_time`        datetime            DEFAULT NULL COMMENT '结束时间',
    `end_time_flag`   tinyint(4)          DEFAULT '1' COMMENT '是否限制结束时间：0-否,1-是',
    `job_status`      tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态: 0-未开始，1-运行中，2-已停止，3-已结束',
    `status`          tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态: 0-无效，1- 有效',
    `create_time`     timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='作业表';
-- ----------------------------
-- Table structure for `t_sub_job`
-- ----------------------------
DROP TABLE IF EXISTS `t_sub_job`;
CREATE TABLE `t_sub_job`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '子作业表ID(自增长)',
    `job_id`         bigint(20)          DEFAULT NULL COMMENT '作业表id',
    `workflow_id`    bigint(20)          DEFAULT NULL COMMENT '工作流id',
    `begin_time`     datetime            DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `end_time`       datetime            DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
    `run_time`       varchar(10)         DEFAULT NULL COMMENT '运行时长',
    `sub_job_status` tinyint(4)          DEFAULT NULL COMMENT '作业状态:0-未开始,1-运行中,2-运行成功,3-运行失败',
    `status`         tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY (`job_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='子作业表';

-- ----------------------------
-- Table structure for `t_sub_job_node`
-- ----------------------------
DROP TABLE IF EXISTS `t_sub_job_node`;
CREATE TABLE `t_sub_job_node`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务表ID(自增长)',
    `sub_job_id`   bigint(20)          DEFAULT NULL COMMENT '子作业表id',
    `algorithm_id` bigint(20)          DEFAULT NULL COMMENT '算法id',
    `node_step`    int(11)             DEFAULT NULL COMMENT '节点在工作流中序号,从1开始',
    `run_status`   tinyint(4)          DEFAULT NULL COMMENT '运行状态:0-未开始,1-运行中,2-运行成功,3-运行失败',
    `task_id`      varchar(256)        DEFAULT NULL COMMENT '任务ID,底层处理完成后返回',
    `run_msg`      varchar(256)        DEFAULT NULL COMMENT '任务处理结果描述',
    `status`       tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `SUB_JOB_ALG_ID` (`sub_job_id`, `algorithm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='子作业节点表';

-- ----------------------------
-- Table structure for `t_task_event`
-- 是否需要从rosettanet同步待确认
-- ----------------------------
DROP TABLE IF EXISTS `t_task_event`;
CREATE TABLE `t_task_event`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '任务表ID(自增长)',
    `task_id`     varchar(256) NOT NULL COMMENT '任务ID',
    `type`        varchar(20)  NOT NULL COMMENT '事件类型',
    `identity_id` varchar(128) NOT NULL COMMENT '产生事件的组织身份ID',
    `event_time`  datetime     NOT NULL COMMENT '产生事件的时间',
    `content`     varchar(512) NOT NULL COMMENT '事件内容',
    `status`      tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务事件表';

-- ----------------------------
-- Table structure for `t_task_result`
-- ----------------------------
DROP TABLE IF EXISTS `t_task_result`;
CREATE TABLE `t_task_result`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '任务运行结果表ID(自增长)',
    `task_id`     varchar(256) NOT NULL COMMENT '任务ID',
    `file_name`   varchar(128) NOT NULL COMMENT '任务结果文件的名称',
    `metadata_id` varchar(128) NOT NULL COMMENT '任务结果文件的元数据Id <系统默认生成的元数据>',
    `origin_id`   varchar(128) NOT NULL COMMENT '任务结果文件的原始文件Id',
    `file_path`   varchar(256) NOT NULL COMMENT '任务结果文件的完整相对路径名',
    `ip`          varchar(32)  NOT NULL COMMENT '任务结果文件所在的 数据服务内网ip',
    `port`        varchar(8)   NOT NULL COMMENT '任务结果文件所在的 数据服务内网port',
    `status`      tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态: 0-无效，1- 有效',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY UK_TASK_RESULT (`task_id`, `metadata_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务运行结果表';
