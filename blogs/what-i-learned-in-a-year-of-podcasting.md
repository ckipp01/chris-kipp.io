---
title: What I Learned in a Year of Podcasting
date: 2022-07-18
description: What I learned in the first year of starting a podcast, and commonly asked questions.
---

# What I learned in the first year of starting a podcast, and commonly asked questions.

My hope is that this post will serve two purposes:
  
1. Be a reflection on starting and doing a
   [podcast](https://www.tooling-talks.com/) for a year on what I'd consider a
   pretty niche topic, [Scala](https://scala-lang.org/) developer Tooling.
2. Be a collection of answers to commonly asked questions I get about starting a
   podcast, in hopes to help others on their journey.

Feel free to jump down to the section that interests you the most, or just keep
reading.

- [How do you pick a topic?](#how-do-you-pick-a-topic)
- [How do you even make a podcast?](#how-do-you-even-make-a-podcast)
- [What do you need?](#what-do-you-need)
- [What does it cost?](#what-does-it-cost)
- [How much time does it take?](#how-much-time-does-it-take)
- [How do you invite guests?](#how-do-you-invite-guests)
- [How do you control quality?](#how-do-you-control-quality)

## [How do you pick a topic](#how-do-you-pick-a-topic)?

When I first wanted to start a podcast, it was for a variety of reasons that
probably differ a bit per individual, but one of the main reasons for me was
that throughout Covid my interaction with people was almost entirely limited to
online activity. It was really during that time for me that I got even more
heavily involved in the Scala community, and starting really connecting with
people that I was interacting with on a daily basis, digitally. With that being
said, they were just drive-by interactions, short chats on Discord, or even just
_thumbs up_ on GitHib issues. I often heard others fondly talk about chatting
with friends at conferences, skipping a session and hacking on something
together, or just in general having conversations about the interests you share
that not many of your direct in-person circles do. I longed for that experience,
and thought maybe starting a podcast talking about something I deeply cared
about and spent an exuberant amount of time on was a good idea.

The practical advice I pull out of that is that if you're going to start a
podcast, start it about something you care about. Start it about something you
don't mind spending hours of your weekend time researching about, or
re-listening to recorded episodes as you edit, or search around for new topics
to talk about. We'll get into this more later, but it also helps to be a part of
the community of the thing you're going to be speaking about. This helps
tremendously with finding new guests, hearing about new interesting topics you
can cover, and to help spread the word around about this new show that you're
putting effort into. You also want to make sure there is enough substance and
options to talk about in this topic. This is all made easier when you're
actively participating in that community.

It's also worth mentioning that when thinking about your topic, you should also
put some thought into your audience. While it may seem obvious, if you pick a
more niche topic you need to know that you're working with a much smaller pool
of people that will probably engage in your podcast in a very different way.
Here are some things I considered when thinking about my potential audience when
I started [Tooling Talks](https://www.tooling-talks.com/).

- Tooling is a more niche topic than Scala in general, so I'm already
   limiting my audience compared to doing a more generic Scala podcast.
- Tooling is something most new developers don't think or really care that much
    about if it's doing it's job. How can I make sure this podcast is also
    something a newcomer to the ecosystem could listen to and gain from?
- Anecdotally, I see less male-identifying individuals in Scala tooling than
    other ecosystems, how can I ensure my podcast reaches a more diverse
    audience than if I otherwise wouldn't think about this.

These are still questions I often think about when I look at the trends in
listeners, the amount of listeners I have, or when inviting new guests.

## [How do you even make a podcast](#how-do-you-even-make-a-podcast)?

Thankfully, this is a topic pretty well covered out there, so I won't go too
much into the technical details, but will give an overview of some of the
choices I made here.

I find that most of the confusion about a podcast comes from distribution, not
the actual "making" the podcast. Once you have a recording, that recording needs
to somehow be distributed to the various services that people listen to. Your
podcast is really just an [rss feed](https://en.wikipedia.org/wiki/Web_feed)
that needs to be hosted so that people can subscribe to or that services like
Spotify or Deezer can subscribe to in order to distribute your podcast. Apart
form your actual feed you'd also need to actual host your audio files in a place
the feed can access. You'd also then need to go to every service you want to
distribute your podcast to and register your podcast with them so they know to
follow your rss feed. While you can do all this manually, it's not something I'd
recommend. There are plenty of services that offer to do all of this for you.
Apart from just hosting your actual podcast and rss feed, they also make it much
easier to distribute your podcast to other services, they offer embedded players
for your podcast episodes, and some even offer you a simple landing page to act
as a website. I personally use [Buzzsprout](https://www.buzzsprout.com/) for
this, which as been fantastic. I even have a
[website](https://tooling-talks.buzzsprout.com/) with them even though I never
link it, since I prefer my own minimal [site](https://www.tooling-talks.com/).
There are a bunch of options here, and the search that you should be doing is
"what's the best place for me to host my podcast". Since there's abundant
articles out there for this, I'll leave that research up to you, but instead
leave you with a few things I considered when I chose to use Buzzsprout.

- What's the cost? Most of these do cost a bit of money, so what's an actual
    affordable option.
- Which offers a flexible embedded player? Like I mentioned, I wanted to host my
    own site, so this mattered to me.
- Which offered the easiest way to distribute my podcast to the various places,
    like Spotify, Google podcasts, etc. If I could just click a button, that'd
    be awesome.
- Which is commonly reviewed as one of the easiest to use? I don't want some
    no-name cheaper alternative that ends up sucking.

## [What do you need](#what-do-you-need)?

If we wanted to completely simplify this section, you could just say:

- A way to record yourself and your guest
- A place to upload your recordings and your rss feed

However, that's pretty radically over-simplified. I'll break down what I chose
to get and why.

### [Recording](#recording)

Arguably the most important thing in making a podcast is audio quality. I have
an entire section on this down below, but this often translates to three things:

1. Getting a better microphone to connect to your recording device
2. Using recording-specific software to help with noise reduction and quality
3. Using some type of post-production software to edit your podcast

Having an external microphone will almost always sound better than using your
built-in mic. I think there is some confusion here where people assume that this
is always very expensive, but there are mics in the under $100 range that will
_greatly_ improve your audio quality. The nice thing is that investing in a mic
here doesn't just need to be for your podcast, but can also benefit you on work
calls, recordings, or anything else you might want to use a mic for. I wouldn't
recommend going out right away and dropping a ton of money here, but getting an
affordable external mic here will help. Keep in mind that even with a nice mic,
a crappy environment or incorrect usage can totally negate any gains here.
Again, we'll talk about controlling quality down below.

There are multiple ways to record your podcast. Some of the most obvious are
just to recording your Google Meet call, your Zoom audio, or whatever software
you're choosing to use. This is for sure the cheapest, but not the highest
quality. There is also software specifically made for recording which can do
things like recording on separate audio tracks or local buffering that can
really help the quality of your sound and ease post-production. I use
[Squadcast](https://squadcast.fm/) for this and have had a great experience.

For editing I use [Audacity](https://www.audacityteam.org/). There are a ton of
other more-advanced tools out there for this, but if you want a tool that can
normalize your audio, easily split and join clips, and make transitions with,
then Audacity is great. It's also open source and free, which was a nice bonus
for me. There is actually a ton of useful articles about using Audacity on the
[Buzzsprout blog](https://www.buzzsprout.com/blog/category/podcast-editing).

### [Hosting and Distributing](#hosting-and-distributing)

I already covered this a bit up above, but I host my podcast on
[Buzzsprout](https://www.buzzsprout.com/). One of the main features I wanted was
to be able to embed an episode. Here's what that looks like for me:

<div id="buzzsprout-player-10348256"></div><script src="https://www.buzzsprout.com/1830936/10348256-amina-adewusi-let-s-talk-about-newcomers.js?container_id=buzzsprout-player-10348256&player=small" type="text/javascript" charset="utf-8"></script>

I also wanted to be able to style it to match my website, which was pretty easy
to do. Since I wanted to host these, I also needed a website, which I was able
to [make myself](https://github.com/ckipp01/tooling-talks)(with Scala) and host
on [Vercel](https://vercel.com/). Again, if you don't want to do this part, it's
much easier to just use the built-in website that many podcast hosting sites
will provide for you.

Since I use Buzzsprout, distribution was also pretty easy. Most of them were
just button clicks, but there were a couple more that involved going to their
respective sites and registering, and sometimes submitting the feed manually. I
have my podcast being circulated on around 20 different site mostly thanks to
how easy the process was.

I haven't mentioned it yet, but if you host your own site, you obviously need a
domain name. Here is also the part you could talk some about branding, using a
dedicated Twitter account, etc. I made some opinionated choices here to not
really brand it too separately from myself. Mainly because I didn't want to have
to deal with it feeling like a "brand" or separate project, but just an
extension of myself. This is why I don't have a separate dedicated Twitter
account for Tooling Talks, a separate way to support the show, and why the
branding on the site looks very similar to my personal site.

## [What does it cost](#what-does-it-cost)?

So this section can be radically different depending on how much you want to
invest in this. You could get by with a pretty minimal budget here if needed,
but to give you a real idea, I've outlined the setup and regular costs for
[Tooling Talks](https://www.tooling-talks.com/) down below.

### [Setup costs](#setup-costs)

Cost      Item                                                                 
--------  ---------------------------------------------------------------------
$14.16    Domain name and ICANN fee - Purchased for one year via [Namecheap](https://www.namecheap.com/)
$0        Hosting on [Vercel](https://vercel.com/) - The free account is fine for this
$24.63    Logo for podcast - Ordered via [Fiverr](https://www.fiverr.com/)
€255      Creative Bundle (mic and interface) - Included a [RØDE NT1-A](https://rode.com/en/microphones/studio-condenser/nt1a) and a [Focusrite Scarlett Solo](https://focusrite.com/en/audio-interface/scarlett/scarlett-solo). Both of these I can use for streaming and for work calls.
€34.99    [Mic arm](https://www.tonormic.com/products/tonor-t20-mic-arm-stand) - Not necessary but depends on your mic and greatly helps with placement. 


### [Monthly costs](#setup-costs)

Cost      Item                                
--------  ------------------------------------
€7        Avatar for guest - Again, from the same artist on Fiverr. Price fluctuates a bit due to tipping.
$12       Monthly cost to host on Buzzsprout - They do have a free account, but it only keeps your episodes for 90 days.
$20       Monthly cost for Squadcast - This is the cheapest tier they have.


One way to offset these costs is to provide a way for people to support the show
financially. This can be through [Patreon](https://www.patreon.com/),
[ko-fi](https://ko-fi.com/), or even [GitHub
Sponsors](https://github.com/sponsors/ckipp01). I used ko-fi for a while, but
after about 3/4 of the year of having it I decided to just close it down. I had
gotten a couple one-off gifts there, but I also felt pressured to add regular
updates their to justify it. Since I didn't want to do that, I just opted for my
personal GitHub Sponsors page since that's where most of my activity can be seen
anyways.

## [How much time does it take?](#how-much-time-does-it-take)

This is arguably as important or more important than the actual monthly
financial cost to have a podcast. Depending on how much you value your time,
this is where the cost really comes in, and also why it's important that you
enjoy the topic you chose in the first place. While I've found that it can vary
a bit on the guest, the quality of the recording, etc, here is how I end up
breaking down the expected time an episode will take me. Note that some of these
are hard to quantify since some of my guests I know really well and it's just a
simple message, where others I've had to hunt down their contact info by
reaching out to mutual contacts, cold emailing, Twitter DMing, etc. The same
goes for preparing since if I'm going to interview someone about
[Metals](https://scalameta.org/metals/) there is way less prep needed than for
example when I spoke to [Rebecca about
Unison](https://www.tooling-talks.com/episode-6). So consider all of the vales
down below as averages.


Time       Activity                                                              
---------  ------------------------------------------------------------
1 Hour     Figuring out how to contact and actually inviting, and fully planning the episode (average)
3 Hours    Playing around with the project we'll talk about or doing research on it
1.5 Hours  The actual recording of the episode
2 Hours    Editing the podcast. It typically takes me about 2 times the episode length to edit it
.5 Hour    Writing the intro, update the website, communicating with the avatar artist

8 hours per episode is probably a pretty accurate estimate of the average time I
spend on a single episode. Originally, I was really surprised by this as I
didn't really expect an episode to take this long. It hopefully puts it into
perspective the amount of time that goes into making episodes. NOTE, my episodes
are *long*. They don't need to be this long. If I'd aim for like a 45 min
average, this would shave off hours of time.

## [How do you invite guests?](#how-do-you-invite-guests)

This is something I still feel like I haven't nailed down. In theory the ideal
way this goes for me is:

1. Find someone I'd really like to do an episode with.
2. Shoot a message to the person asking them to be on the show by
   [showing](https://www.tooling-talks.com/) them who's been on previously, and
   link the
   [process](https://github.com/ckipp01/tooling-talks/blob/main/process.md) I
   follow so they know what to expect.
3. Once accepted I send them a [Squadcast](https://squadcast.fm/) invite so they
   can actually put something on their calendar.
4. About a week before we interview I'll send them a list of questions likely to
   appear when we talk.

And that's it. The fist step of identifying a guest in my opinion is pretty
easy, but the reaching them is not if they're outside of your network. The
guests I've had on the show that I've already interacted with in the past were
super easy to invite. I had their contact info, they recognized my name, and
they were quick to respond with a yes. Then we go one layer out from that and we
have people that I've never really spoken with, but we run in the same circles
so they at least recognized my name, or maybe even have heard of the show
before. Sometimes these were a bit harder since Twitter DMs are a terrible way
to get a hold of someone. Most people you try to contact this way won't even see
your message. The difficult part is that sometimes this is the only public way a
person has to contact them. Sometimes this an oversight, but sometimes is
intentional. This becomes extremely difficult when you're trying to reach
someone outside of your network and in a different ecosystem for example. There
are a handful of people I've had on my list to have on the show since I've
started, and some of them I've never been able to figure out how to contact, as
I don't even have a shared connection with them, and some of them have just
ghosted me even when I found emails or other forms of contact. I think it's
something you just sort of need to accept, that not everyone wants to be on a
podcast, and that's ok. If I find the person's contact info and they don't
respond, I give it a couple months and then try again. If they don't respond the
second time, I move on unless I come across a mutual contact that is willing to
make an introduction.

## [How do you control quality?](#how-do-you-control-quality)

This is another one I'm still working on. Especially if you have guests that
aren't local to you and you're doing your interviews remotely, there isn't a lot
you can control on your guests end. With that being said, this is also a point
that shouldn't dissuade your guest from being a guest. I don't want to lose out
on a potentially great guest just because they don't have an external mic. There
are other tips to help their quality as well. Here are some tips that I [send
ahead of
time](https://github.com/ckipp01/tooling-talks/blob/main/recording-tips.md) to
my guests, and also some things I do to help maintain the quality of sound:

- Investing in software like [Squadcast](https://squadcast.fm/). Squadcast for
    sure isn't cheap, but it offers a couple things that really help. Firstly,
    it buffers locally before upload so even if the internet gets crappy you're
    not losing sound quality. Secondly, the audio tracks are separated which
    makes it easier to edit a ticking noise on one side or loud bangs or stuff
    like that. Investing in some type of software that offers these things are a
    good way to help your own post-production and also help your guest not
    stress if they don't have a great internet connection.
- Make sure your guest is wearing headphones. This will help with cutting out
    any echo that might be picked up from their speakers. Any headphones will
    do. I personally use a pair of [Beyerdynamic DT-770
    Pros](https://north-america.beyerdynamic.com/dt-770-pro.html).
- Encourage your guest to not sit in a giant empty room. If you're basically
    sitting in an empty room, your sound will sound like it was recorded in a
    box. Having stuff around you to dampen the sound really helps.
- Encourage your guest to be mindful of loud water bottles or things on their
    desk. This is an important one that can kill post-production time by having
    to go through and edit all these noises out. Metal water bottles with screw
    caps, clicky pens, and all sorts of things someone could play with on their
    desk can make noise and be picked up by their mic, especially if they are
    using their built-in mic or a cheap external mic.


I hope this all helps you on your podcast journey, or just gives you some
insight into mine. I've really enjoyed talking with all my guests, and have also
really learned to appreciate it when I hear quality podcasts since I know
first-hand the effort it takes. If you're considering making a podcast, but
still have unanswered questions, don't hesitate to reach out.

Thanks for stopping by.
