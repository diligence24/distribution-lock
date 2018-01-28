package yzf.project

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * @author created by yzf on 26/01/2018
 */
@Configuration
@ComponentScan("yzf.project.distributionlock.redis")
open class LockTestContext