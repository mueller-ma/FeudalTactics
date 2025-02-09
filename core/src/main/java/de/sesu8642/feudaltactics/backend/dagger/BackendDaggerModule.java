// SPDX-License-Identifier: GPL-3.0-or-later

package de.sesu8642.feudaltactics.backend.dagger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import dagger.Module;
import dagger.Provides;
import de.sesu8642.feudaltactics.backend.dagger.qualifierannotations.AutoSavePrefStore;
import de.sesu8642.feudaltactics.backend.persistence.AutoSaveRepository;
import de.sesu8642.feudaltactics.frontend.dagger.qualifierannotations.PreferencesPrefixProperty;

/** Dagger module for the backend. */
@Module
public class BackendDaggerModule {

	private BackendDaggerModule() {
		// prevent instantiation
		throw new AssertionError();
	}

	@Provides
	@Singleton
	static EventBus provideEventBus() {
		return new EventBus(new SubscriberExceptionHandler() {
			@Override
			public void handleException(Throwable exception, SubscriberExceptionContext context) {
				Logger logger = LoggerFactory.getLogger(SubscriberExceptionHandler.class.getName());
				logger.error(String.format(
						"an unexpected error happened while handling the event %s in method %s of subscriber %s",
						context.getEvent(), context.getSubscriberMethod(), context.getSubscriber()), exception);
			}
		});
	}

	@Provides
	@Singleton
	static ExecutorService provideBotAiExecutor() {
		return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("botai-%d").build());
	}

	@Provides
	@Singleton
	@AutoSavePrefStore
	// TODO: the prefix should not come from the frontend module
	static Preferences provideAutoSavePrefStore(@PreferencesPrefixProperty String prefix) {
		return Gdx.app.getPreferences(prefix + AutoSaveRepository.AUTO_SAVE_PREFERENCES_NAME);
	}

}