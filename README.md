# SpringDomainCrudify

## Description

### Main features

This artifact is a library that, combined to the Spring Boot Framework, offers features to easily build an API which is domain oriented.
The main features offered by this artifact are :
 * Support for main databases system to store entities (For now, only mongoDB is supported)
 * Multitenancy
 * Built-in objects to enable an Http/Rest API that exposes CRUD endpoints to interact with entities
 * Support for security
   * Authentication based on multiple methods (For now, only Login/Password mechanism is supported)
   * Roles management
   * Endpoints access restriction based on roles 
   * Sessions (For now, only JWT token is supported)
    
### RoadMap

 * Add security to OpenAPI description
 * Implement state machine features
 * Implement SQL Dao
 * Implement Spring AOT
 * Improve the Rest Services returned object => find a way to parametrize the EntityResponse


### Architecture

#### Layers

Controller, WS, Repository

#### Entity VS DTO pojos



####


    
### Note

This artifact is compiled with java 18 Compliance

## Usage

### Import the Spring Domain Crudify 

First step is to create your Spring Boot Project. Then, you can import that artifact in your project.

To import into Maven project, add the following dependency inside `pom.xml`:

	<dependency>
		<groupId>org.sdc</groupId>
		<artifactId>spring-domain-crudify</artifactId>
		<version>1.0.8</version>
	</dependency>

For Gradle users, add these lines inside `build.gradle`:

    dependencies {
        compile group: 'org.sdc', name: 'spring-domain-crudify', version: '1.0.8'
    }
    
### Configuration

The next step is to create the configuration that indicates to the library, the configuration directives.
For that, copy/paste the content of application.properties provided with this library into your own .properties file. 

The basic configuration contained in the application.properties just enables Http/Rest API, nut not Security. 

### Instrument your existing code

As the basic configuration does not activate the security features, you should explicitly indicate to Spring that Security must not be loaded. 

To do so, just add the "exclude = SecurityAutoConfiguration.class" directive to your SpringBootApplication. 
You must also indicate to Spring Boot the Spring Domain Crudify packages to scan in order to let the Dynamic Domain Engine to start. 

Finally your main class should look like the following. 


	@ComponentScan({"com.mypackage", "org.sdc"})
	@Configuration
	@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
	public class Application {
	
		public static void main(String []args) {
			SpringApplication.run(Application.class, args);
		}
	
	}
	
### Develop your domains
Once the project is well set, it is now time to develop your domains. 

#### Use the Dynamic Domain Engine

This way to do is very convenient for building an API based on CRUD endpoints very quickly. But, by this way, CRUD endpoints and software layers cannot be overrided and the Swagger Web UI won't display the endpoints declared by the Dynamic Domain Engine. 

In that way to do, the only things you need to do is :
 * Chose the database (mongodb, ...)
 * Define your domain entities
 * Define your domain DTOs
 
Let's take a simple example : we imagine that we develop an API for booking meeting rooms. It is pretty simple to imagine that our API deals with 2 entities : 
1/ Meeting Room
2/ Booking

##### Entity Meeting Room 

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@SpringCrudifyEntity(dto = "org.sdc.spring.domain.crudify.example.MeetingRoomDTO")
	public class MeetingRoomEntity implements ISpringCrudifyEntity {
	
		private static String domain = "meetingRooms";
	
		@JsonProperty
		private String uuid;
	
		@JsonProperty
		private String id;
		
		@JsonProperty
		private String name;
		
		@JsonProperty
		private String location;
		
		@JsonProperty
		private String[] facilities;
	
		@Override
		public ISpringCrudifyEntityFactory<MeetingRoomEntity> getFactory() {
			ISpringCrudifyEntityFactory<MeetingRoomEntity> factory = new ISpringCrudifyEntityFactory<MeetingRoomEntity>() {
			
				@Override
				public MeetingRoomEntity newInstance() {
					return new MeetingRoomEntity();
				}
	
				@Override
				public MeetingRoomEntity newInstance(String uuid) {
					MeetingRoomEntity entity = new MeetingRoomEntity();
					entity.setUuid(uuid);
					return entity;
				}
			};
			return factory ;
		}
	
		@Override
		public String getDomain() {
			return domain;
		}
	}
	
##### DTO Meeting Room



#### Overrides the layers

This way is a little bit more complicated and takes more time to develop but has the advantage to allow you to enrich and override the behaviour of the built-in CRUB methods. This way allows you to create new Http/Rest Endpoints to enhance new features.



### Activate the Swagger Web Ui 	


