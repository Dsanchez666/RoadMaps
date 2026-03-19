package com.example.roadmap.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.roadmap", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String DOMAIN = "com.example.roadmap.domain..";
    private static final String APPLICATION = "com.example.roadmap.application..";
    private static final String INFRASTRUCTURE = "com.example.roadmap.infrastructure..";
    private static final String WEB = "com.example.roadmap.infrastructure.adapter.in.web..";
    private static final String WEB_DTO = "com.example.roadmap.infrastructure.adapter.in.web.dto..";
    private static final String PERSISTENCE = "com.example.roadmap.infrastructure.adapter.out.persistence..";
    private static final String DB = "com.example.roadmap.infrastructure.db..";

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application_or_infrastructure =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, INFRASTRUCTURE);

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAnyPackage(INFRASTRUCTURE);

    @ArchTest
    static final ArchRule domain_must_not_depend_on_frameworks =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "javax..",
                            "com.fasterxml.."
                    );

    @ArchTest
    static final ArchRule application_must_not_depend_on_frameworks =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "javax..",
                            "com.fasterxml.."
                    );

    @ArchTest
    static final ArchRule adapters_should_reside_in_infrastructure =
            classes().that().resideInAPackage("..adapters..")
                    .should().resideInAPackage(INFRASTRUCTURE)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_should_reside_in_web_adapter =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage(WEB);

    @ArchTest
    static final ArchRule configuration_should_reside_in_infrastructure =
            classes().that().haveSimpleNameEndingWith("Config")
                    .should().resideInAPackage(INFRASTRUCTURE);

    @ArchTest
    static final ArchRule domain_ports_should_reside_in_domain_port =
            classes().that().haveSimpleNameEndingWith("Port")
                    .and().resideInAPackage("..domain..")
                    .should().resideInAnyPackage("..domain.port.in..", "..domain.port.out..");

    @ArchTest
    static final ArchRule application_usecases_should_not_reference_web =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAPackage(WEB);

    @ArchTest
    static final ArchRule web_adapter_should_not_depend_on_persistence_adapter =
            noClasses().that().resideInAPackage(WEB)
                    .should().dependOnClassesThat().resideInAPackage(PERSISTENCE);

    @ArchTest
    static final ArchRule java_sql_only_in_db =
            noClasses().that().resideOutsideOfPackage(DB)
                    .should().dependOnClassesThat().resideInAPackage("java.sql..");

    @ArchTest
    static final ArchRule dto_classes_must_reside_in_web_dto =
            classes().that().haveSimpleNameEndingWith("Request")
                    .or().haveSimpleNameEndingWith("Response")
                    .should().resideInAPackage(WEB_DTO);

    @ArchTest
    static final ArchRule infrastructure_can_depend_on_application_and_domain =
            classes().that().resideInAPackage(INFRASTRUCTURE)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            INFRASTRUCTURE,
                            DOMAIN,
                            APPLICATION,
                            "java..",
                            "javax..",
                            "jakarta..",
                            "org.springframework..",
                            "org.slf4j..",
                            "org.apache..",
                            "com.fasterxml..",
                            "com.tngtech.."
                    );
}
