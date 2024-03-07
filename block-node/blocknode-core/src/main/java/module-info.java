module com.hedera.storage.blocknode.core {
    // Selectively export non-public packages to the test module.
    exports com.hedera.node.blocknode.core;

    // Require the modules needed for compilation.
    requires com.hedera.storage.blocknode.filesystem.local;
    requires com.hedera.storage.blocknode.filesystem.s3;
    requires com.swirlds.config.extensions;
    requires grpc.netty;
    requires grpc.stub;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;
    requires org.apache.logging.log4j;

    // Require modules which are needed for compilation and should be available to all modules that depend on this
    // module (including tests and other source sets).
    requires transitive com.hedera.storage.blocknode.config;
    requires transitive com.hedera.storage.blocknode.core.spi;
    requires transitive com.hedera.storage.blocknode.filesystem.api;
    requires transitive com.hedera.storage.blocknode.grpc.api;
    requires transitive com.hedera.storage.blocknode.state;
    requires transitive com.hedera.node.hapi;
    requires transitive com.hedera.node.app;
    requires static com.github.spotbugs.annotations;
}

