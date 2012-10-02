package org.hubiquitus.hubotsdk.adapters;

import org.hubiquitus.hapi.hStructures.HGeo;
import org.hubiquitus.hapi.hStructures.HLocation;
import org.hubiquitus.hapi.hStructures.HMessage;
import org.hubiquitus.hubotsdk.AdapterInbox;
import org.hubiquitus.hubotsdk.adapters.HtwitterAdapter.HTweet;
import org.hubiquitus.hubotsdk.adapters.HtwitterAdapter.HTweetAuthor;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * the HTwitterAdapterInbox allow you to fetch some tweets from the
 * twitter streaming api
 */
public class HTwitterAdapterInbox extends AdapterInbox{

	final Logger log = LoggerFactory.getLogger(HTwitterAdapterInbox.class);

	private String consumerKey ;
	private String consumerSecret;
	private String twitterAccessToken;
	private String twitterAccessTokenSecret;
	private String langFilter;
	private String tags;

	protected TwitterStream twitterStream;

	@Override
	public void setProperties(JSONObject properties) {
		if(properties != null){
			try{
			if(properties.has("consumerKey")){
				this.consumerKey = properties.getString("consumerKey");
			}
			if (properties.has("consumerSecret")) {
				this.consumerSecret = properties.getString("consumerSecret");
			}
			if (properties.has("twitterAccessToken")) {
				this.twitterAccessToken = properties.getString("twitterAccessToken");
			}
			if (properties.has("twitterAccessTokenSecret")) {
				this.twitterAccessTokenSecret = properties.getString("twitterAccessTokenSecret");
			}
			if (properties.has("langFilter")) {
				this.langFilter = properties.getString("langFilter");
			}
			if (properties.has("tags")) {
				this.tags = properties.getString("tags");
			}
			}catch(Exception e){
				log.debug("message: ",e);
			}
		}
		log.info("Properties Initialized : " + this);

	}

	/**
	 * Function to start Streaming
	 */
	public void start() {
		log.info("Starting...");
		stream();
		log.info("Started.");
	}

	/**
	 * Function to stop Streaming
	 */
	public void stop() {
		log.info("Stopping...");
		twitterStream.shutdown();
		log.info("Stopped.");
	}

	/**
	 * Function for tweet Streaming
	 */
	private void stream() {
		/**
		 * Configuration for access to twitter account
		 */
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setUseSSL(true); 
		cb.setOAuthConsumerKey(consumerKey);
		cb.setOAuthConsumerSecret(consumerSecret);
		cb.setOAuthAccessToken(twitterAccessToken);
		cb.setOAuthAccessTokenSecret(twitterAccessTokenSecret);

		//Instantiation of tweet stream
		twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {
			public void onStatus(Status tweet) {
				String lang = tweet.getUser().getLang();
				//Set the filter for the language
				if ((langFilter == null) || (lang != null && lang.equalsIgnoreCase(langFilter))) {
					HMessage message = transformtweet(tweet);
					put(message);
				}
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

			public void onScrubGeo(long userId, long upToStatusId) {}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};

		FilterQuery fq = new FilterQuery();
		fq.track(tags.split(","));

		twitterStream.addListener(listener);
		twitterStream.filter(fq);  
	}

	/**
	 * Function for transforming Tweet to HMessage
	 * @param tweet from twitter4j
	 * @return HMessage of type hTweet
	 */
	private HMessage transformtweet(Status tweet){
		HMessage message = new HMessage();

		HTweet htweet = new HTweet();
		HTweetAuthor hauthortweet = new HTweetAuthor();

		//Construct the location 
		HLocation location = new HLocation();
		if(tweet.getGeoLocation() != null ) {
            HGeo geo = new HGeo();
            geo.setLat(tweet.getGeoLocation().getLatitude());
            geo.setLng(tweet.getGeoLocation().getLongitude());
			location.setPos(geo);
			message.setLocation(location);
		}

		//Construct the Place
		if(tweet.getPlace()!= null){
			if(tweet.getPlace().getStreetAddress()!= null){
				location.setAddr(tweet.getPlace().getStreetAddress());
			}
			if(tweet.getPlace().getCountryCode()!= null){
				location.setCountryCode(tweet.getPlace().getCountryCode());
			}
			if((tweet.getPlace().getPlaceType()!= null) && ("city".equalsIgnoreCase(tweet.getPlace().getPlaceType()))){
				location.setCity(tweet.getPlace().getName());
			}
		}

		// Init The date
		tweet.getCreatedAt().getTime();
		DateTime createdAt = new DateTime(tweet.getCreatedAt());
		DateTime createdAtAuthor = new DateTime(tweet.getUser().getCreatedAt());
		message.setPublished(new DateTime(createdAt));

		//Construct the Authortweet JSONObject

		hauthortweet.setStatus(tweet.getUser().getStatusesCount());
		hauthortweet.setFollowers(tweet.getUser().getFollowersCount());
		hauthortweet.setFriends(tweet.getUser().getFriendsCount());
		hauthortweet.setLocation(tweet.getUser().getLocation());
		hauthortweet.setDescription(tweet.getUser().getDescription());		
		hauthortweet.setProfileImg(tweet.getUser().getProfileImageURL().toString());
		hauthortweet.setUrl(tweet.getUser().getURL());
		hauthortweet.setCreatedAt(createdAtAuthor);
		hauthortweet.setLang(tweet.getUser().getLang());
		hauthortweet.setListeds(tweet.getUser().getListedCount());
		hauthortweet.setGeo(tweet.getUser().isGeoEnabled());
		hauthortweet.setVerified(tweet.getUser().isVerified());
		hauthortweet.setName(tweet.getUser().getName());

		//Construct the tweet JSONObject		
		htweet.setId(tweet.getId());
		htweet.setSource(tweet.getSource());
		htweet.setText(tweet.getText());
		htweet.setAuthor(hauthortweet);

		message.setPayload(htweet);
		message.setType("hTweet");
		
		message.setAuthor(tweet.getUser().getScreenName() + "@twitter.com");


		if (log.isDebugEnabled()) {
			log.debug("tweet("+tweet+") -> hMessage :"+message);
		}

		return message;
	}


	/* Getters & Setters */

	/*public String getConsumerKey() {
		return consumerKey;
	}*/


	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	/*public String getTags() {
		return tags;
	}*/

	public void setTags(String tags) {
		this.tags = tags;
		if(log.isDebugEnabled()){
			log.debug("Tags updated : " +tags);
		}
	}


	/*
	public String getConsumerSecret() {
		return consumerSecret;
	}*/

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	/*public String getTwitterAccessToken() {
		return twitterAccessToken;
	}*/

	public void setTwitterAccessToken(String twitterAccessToken) {
		this.twitterAccessToken = twitterAccessToken;
	}

	/*public String getTwitterAccessTokenSecret() {
		return twitterAccessTokenSecret;
	}*/

	public void setTwitterAccessTokenSecret(String twitterAccessTokenSecret) {
		this.twitterAccessTokenSecret = twitterAccessTokenSecret;
	}

	/*public String getLangFilter() {
		return langFilter;
	}*/

	public void setLangFilter(String langFilter) {
		this.langFilter = langFilter;
	}

	@Override
	public String toString() {
		return "HTwitterAdapterInbox [consumerKey=" + consumerKey
				+ ", consumerSecret=" + consumerSecret
				+ ", twitterAccessToken=" + twitterAccessToken
				+ ", twitterAccessTokenSecret=" + twitterAccessTokenSecret
				+ ", langFilter=" + langFilter + ", tags=" + tags + "]";
	}



}
