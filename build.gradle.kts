plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

apply(from = "https://raw.githubusercontent.com/JackOfNoneTrades/67minecraft-gradle-publish/${property("publish67ScriptTag")}/67minecraft-publish.gradle.kts")

dependencies {
    compileOnly("ganymedes01.etfuturum:Et-Futurum-Requiem:${property("etFuturumRequiemVersion")}:api") {
        isTransitive = false
    }
}

tasks.withType<Jar>().configureEach {
    from("LICENSE") {
        into("META-INF")
        rename { "EVENMOBENDS-LICENSE.txt" }
    }
    from("LICENSES") {
        into("META-INF/LICENSES")
    }
    from("UPSTREAM.md") {
        into("META-INF")
        rename { "EVENMOBENDS-UPSTREAM.md" }
    }
}
