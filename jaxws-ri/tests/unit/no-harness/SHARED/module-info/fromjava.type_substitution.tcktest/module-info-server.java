module server {
    requires java.xml.ws;
     requires jdk.httpserver;
      requires java.logging; 

    // generated by WebServiceWrapperGenerator
    exports fromjava.type_substitution.tcktest.jaxws;
    exports fromjava.type_substitution.tcktest;
}
