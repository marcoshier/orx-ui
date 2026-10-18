package lib

import com.typesafe.config.ConfigRenderOptions

val hoconConfigOptions = ConfigRenderOptions.defaults()
    .setOriginComments(false)
    .setJson(false)
    .setFormatted(true)