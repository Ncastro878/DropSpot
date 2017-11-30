# DropSpot
My DropSpot app. 
This is my first major project used primarily as a learning experience and my chance to create a neat idea I thought of when I first decided to learn Android Development. 
The basic idea: It is a location/Time-based chatroom app. You can create a chatroom that is centered at your physical GPS location, and anybody that is within approximately 1 mile of where the chatroom was created can join and take part in the chatroom. Thus far, all chatrooms appear on a Google Map in the app, and on a list of created rooms.

However, you are restricted from joining rooms that are 1 mile or farther way from your location. These rooms will be denoted with red pins on the map and a red background on the list of rooms. Allowable rooms will have a green pin and green background on the list of rooms. This is the location centered aspect.

All rooms are also time based. Rooms older than 4 hours are expired and will be deleted from the database and from the app when the first person to click it does so after it officially expires. (The person clicking it triggers a check on its age, I'm working on routine self-checks/deletion in the actual database - which is a bit harder using Google Firebase Cloud Functions right now).


Tools used/learned in building this app:

-Google FireBase for Android

-Android Location Request

-Fragments/RecyclerView/ViewPager/PagerAdapter

-GeoFire*

![](/Screenshot_20171127-233142-iloveimg-resized.png)

![](/Screenshot_20171028-030129.png)


*At this point, GeoFire is unnecessary but it sparked ideas, was fun to learn, and may prove useful soon to limit the which rooms show up on the map and list of chatrooms, i.e. only show chatrooms within 20 miles vs all chatrooms currently created on Earth. 
